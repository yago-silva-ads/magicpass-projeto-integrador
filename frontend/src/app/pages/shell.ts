import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/api';
import { Estrela, Icone } from '../core/icone';

/** Layout do sistema logado: menu lateral + conteudo. */
@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Icone, Estrela],
  template: `
    <div class="layout">
      <aside class="menu">
        <a class="logo" routerLink="/painel"><app-estrela [tamanho]="28" /> <span class="logo-texto">MagicPass<small>Viagens</small></span></a>
        <nav>
          @for (item of principal; track item.rota) {
            <a [routerLink]="item.rota" routerLinkActive="ativo"><app-icone [nome]="item.icone" />{{ item.rotulo }}</a>
          }
          <span class="secao">Catalogo</span>
          @for (item of catalogo; track item.rota) {
            <a [routerLink]="item.rota" routerLinkActive="ativo"><app-icone [nome]="item.icone" />{{ item.rotulo }}</a>
          }
          @if (auth.admin) {
            <span class="secao">Administracao</span>
            <a routerLink="/usuarios" routerLinkActive="ativo"><app-icone nome="usuarios" />Usuarios</a>
          }
          <span class="secao">Publico</span>
          <a routerLink="/black-friday" target="_blank"><app-icone nome="vitrine" />Vitrine Black Friday <app-icone class="ext" nome="externo" [tamanho]="13" /></a>
        </nav>
        <div class="usuario">
          <span class="avatar">{{ inicial() }}</span>
          <div>
            <b>{{ auth.sessao()?.nome }}</b>
            <small>{{ auth.admin ? 'Administrador' : 'Vendedor' }}</small>
          </div>
          <button class="btn-icone" title="Sair" (click)="auth.sair()"><app-icone nome="sair" /></button>
        </div>
      </aside>
      <main class="conteudo"><router-outlet /></main>
    </div>
  `,
})
export class Shell {
  readonly auth = inject(AuthService);
  readonly principal = [
    { rota: '/painel', rotulo: 'Painel', icone: 'painel' },
    { rota: '/vendas', rotulo: 'Vendas', icone: 'vendas' },
    { rota: '/clientes', rotulo: 'Clientes', icone: 'clientes' },
  ];
  readonly catalogo = [
    { rota: '/produtos', rotulo: 'Ingressos e Pacotes', icone: 'ingresso' },
    { rota: '/parques', rotulo: 'Parques', icone: 'castelo' },
    { rota: '/parceiros', rotulo: 'Parceiros', icone: 'parceiros' },
    { rota: '/promocoes', rotulo: 'Promocoes', icone: 'promocao' },
  ];

  inicial() {
    return (this.auth.sessao()?.nome ?? '?').trim().charAt(0).toUpperCase();
  }
}
