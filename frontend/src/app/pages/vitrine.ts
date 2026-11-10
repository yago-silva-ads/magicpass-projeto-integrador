import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Revelar } from '../core/animacoes';
import { ApiService, AuthService, lerErro } from '../core/api';
import { CREDITOS_FOTOS } from '../core/creditos';
import { Estrela, Icone } from '../core/icone';
import { Oferta, Vitrine } from '../core/modelos';

/** Foto padrao por parque (usada se o cadastro nao tiver imagem). */
const FOTO_PARQUE: Record<string, string> = {
  'Magic Kingdom': 'img/magic-kingdom.jpg',
  EPCOT: 'img/epcot.jpg',
  'Hollywood Studios': 'img/hollywood-studios.jpg',
  'Animal Kingdom': 'img/animal-kingdom.jpg',
  'Disneyland Paris': 'img/disneyland-paris.jpg',
  'Disneyland Resort': 'img/disneyland-california.jpg',
};

@Component({
  selector: 'app-vitrine',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink, Revelar, Icone, Estrela],
  templateUrl: './vitrine.html',
})
export class VitrinePage implements OnInit, OnDestroy {
  private api = inject(ApiService);
  private router = inject(Router);
  readonly auth = inject(AuthService);
  readonly creditos = CREDITOS_FOTOS;

  readonly personagens = [
    { foto: 'img/personagem-mickey.jpg', nome: 'Mickey', onde: 'Festival of Fantasy Parade' },
    { foto: 'img/personagem-cinderela.jpg', nome: 'Cinderela e o Principe', onde: 'Princess Garden' },
    { foto: 'img/personagem-rapunzel.jpg', nome: 'Rapunzel', onde: 'Enrolados no desfile' },
    { foto: 'img/personagem-dumbo.jpg', nome: 'Dumbo', onde: 'Carro alegorico do circo' },
    { foto: 'img/personagem-ariel.jpg', nome: 'Ariel', onde: 'A Pequena Sereia' },
  ];

  readonly destinos = [
    { foto: 'img/magic-kingdom.jpg', nome: 'Magic Kingdom', local: 'Orlando' },
    { foto: 'img/epcot-noite.jpg', nome: 'EPCOT', local: 'Orlando' },
    { foto: 'img/animal-kingdom.jpg', nome: 'Animal Kingdom', local: 'Orlando' },
    { foto: 'img/hollywood-studios.jpg', nome: 'Hollywood Studios', local: 'Orlando' },
    { foto: 'img/disneyland-paris.jpg', nome: 'Disneyland Paris', local: 'Franca' },
    { foto: 'img/disneyland-california.jpg', nome: 'Disneyland Resort', local: 'California' },
  ];

  readonly v = signal<Vitrine | null>(null);
  readonly erro = signal('');
  readonly filtro = signal<'TODOS' | 'INGRESSO' | 'PACOTE'>('TODOS');
  readonly ordem = signal<'desconto' | 'preco'>('desconto');
  readonly agora = signal(Date.now());
  readonly copiado = signal('');
  readonly verCreditos = signal(false);
  private relogio?: ReturnType<typeof setInterval>;

  // simulador da viagem em familia
  simProduto: number | null = null;
  readonly simQtd = signal(4);
  readonly simCupom = signal(true);
  readonly simPix = signal(true);
  readonly simSel = signal<number | null>(null);

  readonly ofertas = computed(() => {
    const lista = (this.v()?.ofertas ?? []).filter((o) => this.filtro() === 'TODOS' || o.categoria === this.filtro());
    return [...lista].sort((a, b) =>
      this.ordem() === 'preco' ? a.precoPromocional - b.precoPromocional : b.percentualDesconto - a.percentualDesconto);
  });

  readonly cupom = computed(() => this.v()?.cupons?.[0] ?? null);

  /** "Ate X% OFF": melhor oferta + familia + cupom (limitado a 70%, como no backend). */
  readonly ateOff = computed(() => {
    const v = this.v();
    if (!v || !v.ofertas.length) return 0;
    const melhor = Math.max(...v.ofertas.map((o) => o.percentualDesconto));
    return Math.min(70, melhor + (v.campanha?.percentualProgressivo ?? 0) + (this.cupom()?.percentual ?? 0));
  });

  readonly restante = computed(() => {
    const fim = this.v()?.campanha?.fim;
    if (!fim) return null;
    const ms = Math.max(0, new Date(fim).getTime() - this.agora());
    const s = Math.floor(ms / 1000);
    return { d: Math.floor(s / 86400), h: Math.floor((s % 86400) / 3600), m: Math.floor((s % 3600) / 60), s: s % 60 };
  });

  readonly simulacao = computed(() => {
    const v = this.v();
    const o = v?.ofertas.find((x) => x.produtoId === this.simSel());
    if (!v || !o) return null;
    const qtd = Math.max(1, Math.min(20, Math.floor(Number(this.simQtd()) || 1)));
    let pct = Number(o.percentualDesconto);
    const familia = !!v.campanha && o.percentualDesconto > 0 && v.campanha.qtdMinimaProgressivo > 0 && qtd >= v.campanha.qtdMinimaProgressivo;
    if (familia) pct += Number(v.campanha!.percentualProgressivo);
    if (this.simCupom() && this.cupom()) pct += Number(this.cupom()!.percentual);
    pct = Math.min(70, pct);
    const unit = Math.round(o.precoOriginal * (100 - pct)) / 100;
    let total = Math.round(unit * qtd * 100) / 100;
    const cheio = Math.round(o.precoOriginal * qtd * 100) / 100;
    if (this.simPix()) total = Math.round(total * 95) / 100;
    return { o, qtd, pct, familia, total, cheio, economia: Math.round((cheio - total) * 100) / 100, parcela: Math.round((total / 12) * 100) / 100 };
  });

  async ngOnInit() {
    this.relogio = setInterval(() => this.agora.set(Date.now()), 1000);
    try {
      const v = await this.api.get<Vitrine>('publico/vitrine');
      this.v.set(v);
      const pacote = v.ofertas.find((o) => o.categoria === 'PACOTE') ?? v.ofertas[0];
      if (pacote) { this.simProduto = pacote.produtoId; this.simSel.set(pacote.produtoId); }
    } catch (e) {
      this.erro.set(lerErro(e).mensagem);
    }
  }

  ngOnDestroy() {
    clearInterval(this.relogio);
  }

  foto(o: Oferta) {
    return o.imagemUrl || FOTO_PARQUE[o.parque] || 'img/hero-castelo-fogos.jpg';
  }

  doisDigitos(n: number) {
    return String(n).padStart(2, '0');
  }

  precoPix(o: Oferta) {
    return Math.round(o.precoPromocional * 95) / 100;
  }

  async copiar(codigo: string) {
    try { await navigator.clipboard.writeText(codigo); } catch { /* navegador sem permissao */ }
    this.copiado.set(codigo);
    setTimeout(() => this.copiado.set(''), 2000);
  }

  irPara(id: string) {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  reservar(o: Oferta) {
    const destino = `/vendas/nova?produto=${o.produtoId}`;
    if (this.auth.logado) this.router.navigateByUrl(destino);
    else this.router.navigate(['/login'], { queryParams: { voltar: destino } });
  }
}
