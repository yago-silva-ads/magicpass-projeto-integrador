import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Icone } from '../core/icone';
import { ApiService, ToastService } from '../core/api';
import { Entidade } from '../core/modelos';
import { mascaraCpf } from '../core/validadores';

const PAGAMENTO: Record<string, string> = { PIX: 'PIX', CARTAO_CREDITO: 'Cartao', BOLETO: 'Boleto' };

@Component({
  selector: 'app-vendas',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink, Icone],
  template: `
    <section class="pagina">
      <header class="pagina-topo">
        <div>
          <h1><span class="titulo-icone"><app-icone nome="vendas" [tamanho]="22" /></span>Vendas</h1>
          <p class="sub">Ingressos e pacotes vendidos (cancelar devolve o estoque)</p>
        </div>
        <div class="acoes-topo">
          <label class="busca-wrap"><app-icone nome="busca" [tamanho]="16" /><input class="busca" type="search" placeholder="Cliente, CPF ou produto..." [ngModel]="busca()" (ngModelChange)="busca.set($event)" /></label>
          <a class="btn primario" routerLink="/vendas/nova"><app-icone nome="mais" [tamanho]="16" /> Nova venda</a>
        </div>
      </header>

      <div class="cartao tabela-wrap">
        @if (carregando()) { <p class="vazio">Carregando...</p> }
        @else if (filtradas().length === 0) { <p class="vazio">Nenhuma venda encontrada.</p> }
        @else {
          <table class="tabela">
            <thead><tr><th>#</th><th>Data</th><th>Cliente</th><th>Itens</th><th>Pagamento</th><th class="dir">Desconto</th><th class="dir">Total</th><th>Status</th><th class="dir">Acoes</th></tr></thead>
            <tbody>
              @for (v of filtradas(); track v.id) {
                <tr class="linha-anim" [style.--i]="$index" [class.cancelada]="v['status'] === 'CANCELADA'">
                  <td class="muted">{{ v.id }}</td>
                  <td class="nowrap">{{ v['dataVenda'] | date: 'dd/MM/yy HH:mm' }}</td>
                  <td>{{ v['cliente']?.nome }}<br /><small class="muted">{{ cpf(v['cliente']?.cpf) }}</small></td>
                  <td>
                    @for (i of v['itens']; track i.id) { <div class="item-mini">{{ i.quantidade }}x {{ i.produto?.nome }} <small class="muted">({{ i.dataVisita | date: 'dd/MM/yy' }})</small></div> }
                    @if (v['promocoesAplicadas']) { <small class="promo-mini"><app-icone nome="promocao" [tamanho]="12" /> {{ v['promocoesAplicadas'] }}</small> }
                  </td>
                  <td>{{ pagamento(v) }}</td>
                  <td class="dir verde">-{{ v['desconto'] | currency: 'BRL' }}</td>
                  <td class="dir"><b>{{ v['total'] | currency: 'BRL' }}</b></td>
                  <td><span class="badge" [class.ok]="v['status'] === 'CONFIRMADA'" [class.off]="v['status'] === 'CANCELADA'">{{ v['status'] === 'CONFIRMADA' ? 'Confirmada' : 'Cancelada' }}</span></td>
                  <td class="dir nowrap">
                    @if (v['status'] === 'CONFIRMADA') {
                      <a class="btn mini" [routerLink]="['/vendas', v.id]">Editar</a>
                      <button class="btn mini" (click)="acao.set({ tipo: 'cancelar', venda: v })">Cancelar</button>
                    }
                    <button class="btn mini perigo" (click)="acao.set({ tipo: 'excluir', venda: v })">Excluir</button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        }
      </div>
      <p class="rodape-tabela">{{ filtradas().length }} venda(s) · Total confirmado: <b>{{ totalConfirmado() | currency: 'BRL' }}</b></p>
    </section>

    @if (acao(); as a) {
      <div class="modal-fundo" (click)="acao.set(null)">
        <div class="modal pequeno" (click)="$event.stopPropagation()">
          <h2>{{ a.tipo === 'cancelar' ? 'Cancelar' : 'Excluir' }} venda #{{ a.venda.id }}?</h2>
          <p>
            Cliente <b>{{ a.venda['cliente']?.nome }}</b> · {{ a.venda['total'] | currency: 'BRL' }}.
            @if (a.venda['status'] === 'CONFIRMADA') { Os itens voltam para o estoque. }
            @if (a.tipo === 'excluir') { O registro sera apagado definitivamente. }
          </p>
          <div class="modal-acoes">
            <button class="btn" (click)="acao.set(null)">Voltar</button>
            <button class="btn perigo" (click)="confirmar()">Confirmar</button>
          </div>
        </div>
      </div>
    }
  `,
})
export class VendasPage implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  readonly vendas = signal<Entidade[]>([]);
  readonly carregando = signal(true);
  readonly busca = signal('');
  readonly acao = signal<{ tipo: 'cancelar' | 'excluir'; venda: Entidade } | null>(null);

  readonly filtradas = computed(() => {
    const t = this.busca().trim().toLowerCase();
    if (!t) return this.vendas();
    return this.vendas().filter((v) =>
      [v['cliente']?.nome, v['cliente']?.cpf, ...(v['itens'] ?? []).map((i: any) => i.produto?.nome), String(v.id)]
        .some((x) => String(x ?? '').toLowerCase().includes(t)));
  });

  readonly totalConfirmado = computed(() =>
    this.filtradas().filter((v) => v['status'] === 'CONFIRMADA').reduce((s, v) => s + Number(v['total']), 0));

  ngOnInit() { this.carregar(); }

  async carregar() {
    this.carregando.set(true);
    try { this.vendas.set(await this.api.listar('vendas')); }
    catch (e) { this.toast.erro(e); }
    finally { this.carregando.set(false); }
  }

  cpf(v: string) { return mascaraCpf(v); }

  pagamento(v: Entidade) {
    const f = PAGAMENTO[v['formaPagamento']] ?? v['formaPagamento'];
    return v['parcelas'] > 1 ? `${f} ${v['parcelas']}x` : f;
  }

  async confirmar() {
    const a = this.acao();
    if (!a) return;
    try {
      if (a.tipo === 'cancelar') {
        await this.api.put(`vendas/${a.venda.id}/cancelar`, {});
        this.toast.ok('Venda cancelada e estoque devolvido');
      } else {
        await this.api.excluir('vendas', a.venda.id!);
        this.toast.ok('Venda excluida');
      }
    } catch (e) {
      this.toast.erro(e);
    } finally {
      this.acao.set(null);
      this.carregar();
    }
  }
}
