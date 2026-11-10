import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService, AuthService, ToastService } from '../core/api';
import { Contador, Revelar } from '../core/animacoes';
import { Icone } from '../core/icone';
import { Dashboard } from '../core/modelos';

@Component({
  selector: 'app-painel',
  imports: [CurrencyPipe, DatePipe, RouterLink, Icone, Revelar, Contador],
  template: `
    <section class="pagina">
      <header class="pagina-topo">
        <div>
          <h1>Ola, {{ primeiroNome() }}</h1>
          <p class="sub">Resumo das vendas e da campanha em andamento</p>
        </div>
        <a class="btn primario" routerLink="/vendas/nova"><app-icone nome="mais" [tamanho]="16" /> Nova venda</a>
      </header>

      @if (d(); as d) {
        @if (d.campanhaAtiva; as c) {
          <div class="banner-campanha" revelar>
            <img src="img/hero-castelo-fogos.jpg" alt="" />
            <div class="banner-texto">
              <span class="chip-ouro"><span class="ponto-vivo"></span> No ar agora</span>
              <h2>{{ c.nome }}</h2>
              <p>{{ c.chamada }}</p>
            </div>
            <div class="banner-fim">
              <small>Termina em</small>
              <b>{{ c.fim | date: "dd/MM 'as' HH:mm" }}</b>
              <a class="btn ouro mini" routerLink="/black-friday" target="_blank">Ver vitrine <app-icone nome="externo" [tamanho]="13" /></a>
            </div>
          </div>
        }
        <div class="kpis">
          <div class="kpi" revelar [atraso]="0"><span class="kpi-icone roxo"><app-icone nome="vendas" /></span><small>Faturamento</small><b [contador]="d.faturamento" formato="moeda"></b></div>
          <div class="kpi" revelar [atraso]="60"><span class="kpi-icone azul"><app-icone nome="check" /></span><small>Vendas confirmadas</small><b [contador]="d.vendasConfirmadas"></b><em>{{ d.vendasCanceladas }} canceladas</em></div>
          <div class="kpi" revelar [atraso]="120"><span class="kpi-icone verde"><app-icone nome="trofeu" /></span><small>Ticket medio</small><b [contador]="d.ticketMedio" formato="moeda"></b></div>
          <div class="kpi destaque" revelar [atraso]="180"><span class="kpi-icone ouro"><app-icone nome="promocao" /></span><small>Economia gerada aos clientes</small><b [contador]="d.economiaClientes" formato="moeda"></b></div>
          <div class="kpi" revelar [atraso]="240"><span class="kpi-icone rosa"><app-icone nome="ingresso" /></span><small>Ingressos e pacotes vendidos</small><b [contador]="d.ingressosVendidos"></b></div>
          <div class="kpi" revelar [atraso]="300"><span class="kpi-icone azul"><app-icone nome="clientes" /></span><small>Clientes</small><b [contador]="d.clientes"></b><em>{{ d.produtosAtivos }} produtos ativos</em></div>
        </div>
        <div class="duas-colunas">
          <div class="cartao" revelar>
            <h3><app-icone nome="trofeu" /> Mais vendidos</h3>
            @for (p of d.maisVendidos; track p.produto; let i = $index) {
              <div class="linha-rank">
                <span><b class="posicao">{{ i + 1 }}</b>{{ p.produto }}</span><b>{{ p.quantidade }} un.</b><em>{{ p.faturamento | currency: 'BRL' }}</em>
                <span class="barra" [style.--pct.%]="(p.quantidade / d.maisVendidos[0].quantidade) * 100"></span>
              </div>
            } @empty { <p class="vazio">Nenhuma venda ainda.</p> }
          </div>
          <div class="cartao" revelar [atraso]="80">
            <h3><app-icone nome="alerta" /> Estoque baixo (menos de 10)</h3>
            @for (p of d.estoqueBaixo; track p.id) {
              <div class="linha-rank"><span>{{ p.produto }}</span><b class="badge alerta">{{ p.estoque }}</b></div>
            } @empty { <p class="vazio">Estoque em dia.</p> }
          </div>
        </div>
      } @else {
        <div class="kpis">@for (i of [1,2,3,4,5,6]; track i) { <div class="kpi esqueleto"></div> }</div>
      }
    </section>
  `,
})
export class PainelPage implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  private auth = inject(AuthService);
  readonly d = signal<Dashboard | null>(null);

  primeiroNome() {
    return (this.auth.sessao()?.nome ?? '').split(' ')[0];
  }

  async ngOnInit() {
    try {
      this.d.set(await this.api.get<Dashboard>('dashboard'));
    } catch (e) {
      this.toast.erro(e);
    }
  }
}
