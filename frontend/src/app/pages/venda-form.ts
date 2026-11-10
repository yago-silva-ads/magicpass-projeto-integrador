import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Icone } from '../core/icone';
import { ApiService, lerErro, ToastService } from '../core/api';
import { Cotacao, Entidade, VendaRequest } from '../core/modelos';
import { mascaraCpf } from '../core/validadores';

const hoje = () => new Date().toISOString().slice(0, 10);
const daquiDias = (n: number) => new Date(Date.now() + n * 86400000).toISOString().slice(0, 10);

@Component({
  selector: 'app-venda-form',
  imports: [CurrencyPipe, FormsModule, RouterLink, Icone],
  templateUrl: './venda-form.html',
})
export class VendaFormPage implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private rota = inject(ActivatedRoute);

  readonly vendaId = signal<number | null>(null);
  readonly clientes = signal<Entidade[]>([]);
  readonly produtos = signal<Entidade[]>([]);
  readonly cotacao = signal<Cotacao | null>(null);
  readonly erroCotacao = signal('');
  readonly erros = signal<Record<string, string>>({});
  readonly salvando = signal(false);
  readonly carregado = signal(false);
  readonly hoje = hoje();

  req: VendaRequest = { clienteId: null, formaPagamento: 'PIX', parcelas: 1, cupom: '', observacao: '', itens: [] };
  private timer?: ReturnType<typeof setTimeout>;
  private tentouSalvar = false;

  async ngOnInit() {
    try {
      const [clientes, produtos] = await Promise.all([this.api.listar('clientes'), this.api.listar('produtos')]);
      this.clientes.set(clientes);
      this.produtos.set(produtos.filter((p) => p['ativo']));

      const id = Number(this.rota.snapshot.paramMap.get('id'));
      if (id > 0) {
        const v = await this.api.buscar('vendas', id);
        if (v['status'] === 'CANCELADA') {
          this.toast.erro('Venda cancelada nao pode ser alterada');
          this.router.navigate(['/vendas']);
          return;
        }
        this.vendaId.set(id);
        this.req = {
          clienteId: v['cliente']?.id ?? null,
          formaPagamento: v['formaPagamento'],
          parcelas: v['parcelas'] ?? 1,
          cupom: v['cupom'] ?? '',
          observacao: v['observacao'] ?? '',
          itens: (v['itens'] ?? []).map((i: any) => ({ produtoId: i.produto?.id, quantidade: i.quantidade, dataVisita: i.dataVisita })),
        };
        // produto inativo vendido antes continua aparecendo na edicao
        for (const i of v['itens'] ?? []) {
          if (!this.produtos().some((p) => p.id === i.produto?.id)) this.produtos.update((l) => [...l, i.produto]);
        }
      } else {
        const pre = Number(this.rota.snapshot.queryParamMap.get('produto'));
        this.req.itens = [{ produtoId: pre > 0 && this.produtos().some((p) => p.id === pre) ? pre : null, quantidade: 1, dataVisita: daquiDias(30) }];
      }
      this.carregado.set(true);
      this.recalcular();
    } catch (e) {
      this.toast.erro(e);
      this.router.navigate(['/vendas']);
    }
  }

  cpf(v: string) { return mascaraCpf(v); }

  produto(id: number | null) {
    return this.produtos().find((p) => p.id === id);
  }

  adicionarItem() {
    this.req.itens.push({ produtoId: null, quantidade: 1, dataVisita: daquiDias(30) });
    this.recalcular();
  }

  removerItem(idx: number) {
    if (idx < 0 || idx >= this.req.itens.length) return; // nunca usa indice -1
    this.req.itens.splice(idx, 1);
    this.recalcular();
  }

  mudouPagamento() {
    if (this.req.formaPagamento !== 'CARTAO_CREDITO') this.req.parcelas = 1;
    this.recalcular();
  }

  /** Valida no front antes de chamar a API. */
  private validar(): Record<string, string> {
    const e: Record<string, string> = {};
    if (!this.req.clienteId) e['cliente'] = 'Selecione o cliente';
    if (this.req.itens.length === 0) e['itens'] = 'Adicione pelo menos um item';
    this.req.itens.forEach((i, idx) => {
      if (!i.produtoId) e[`item${idx}`] = 'Selecione o produto';
      else if (!Number.isInteger(Number(i.quantidade)) || Number(i.quantidade) < 1 || Number(i.quantidade) > 20)
        e[`item${idx}`] = 'Quantidade deve ser de 1 a 20';
      else if (!i.dataVisita || i.dataVisita < this.hoje) e[`item${idx}`] = 'Data da visita nao pode estar no passado';
      else {
        const p = this.produto(i.produtoId);
        const totalProduto = this.req.itens.filter((x) => x.produtoId === i.produtoId).reduce((s, x) => s + Number(x.quantidade || 0), 0);
        if (p && !this.vendaId() && totalProduto > p['estoque']) e[`item${idx}`] = `Estoque disponivel: ${p['estoque']}`;
      }
    });
    const parc = Number(this.req.parcelas);
    if (!Number.isInteger(parc) || parc < 1 || parc > 12) e['parcelas'] = 'Parcelas de 1 a 12';
    return e;
  }

  recalcular() {
    clearTimeout(this.timer);
    this.timer = setTimeout(() => this.cotar(), 250);
  }

  private corpo() {
    return {
      ...this.req,
      parcelas: Number(this.req.parcelas) || 1,
      cupom: this.req.cupom?.trim().toUpperCase() || null,
      itens: this.req.itens.map((i) => ({ produtoId: Number(i.produtoId), quantidade: Number(i.quantidade), dataVisita: i.dataVisita })),
    };
  }

  private async cotar() {
    const erros = this.validar();
    if (!this.tentouSalvar) delete erros['cliente']; // so cobra o cliente ao finalizar
    this.erros.set(erros);
    const itensOk = this.req.itens.length > 0 && this.req.itens.every((_, idx) => !erros[`item${idx}`]);
    if (!itensOk) {
      this.cotacao.set(null);
      this.erroCotacao.set('');
      return;
    }
    try {
      this.cotacao.set(await this.api.post<Cotacao>('vendas/cotacao', { ...this.corpo(), clienteId: this.req.clienteId ?? 1 }));
      this.erroCotacao.set('');
    } catch (e) {
      this.cotacao.set(null);
      this.erroCotacao.set(lerErro(e).mensagem);
    }
  }

  async salvar() {
    this.tentouSalvar = true;
    const erros = this.validar();
    this.erros.set(erros);
    if (Object.keys(erros).length) {
      this.toast.erro('Corrija os campos destacados');
      return;
    }
    this.salvando.set(true);
    try {
      const id = this.vendaId();
      if (id) await this.api.atualizar('vendas', id, this.corpo());
      else await this.api.criar('vendas', this.corpo());
      this.toast.ok(id ? 'Venda alterada com sucesso' : 'Venda registrada com sucesso!');
      this.router.navigate(['/vendas']);
    } catch (e) {
      const erro = lerErro(e);
      this.erroCotacao.set(erro.mensagem);
      this.toast.erro(erro.mensagem);
    } finally {
      this.salvando.set(false);
    }
  }
}
