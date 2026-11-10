import { inject } from '@angular/core';
import { CanActivateFn, Router, Routes } from '@angular/router';
import { AuthService } from './core/api';
import { CrudPage } from './pages/crud/crud-page';
import { CLIENTES, PARCEIROS, PARQUES, PRODUTOS, PROMOCOES, USUARIOS } from './pages/crud/crud-config';
import { LoginPage } from './pages/login';
import { PainelPage } from './pages/painel';
import { Shell } from './pages/shell';
import { VendaFormPage } from './pages/venda-form';
import { VendasPage } from './pages/vendas';
import { VitrinePage } from './pages/vitrine';

/** So entra logado; senao vai para o login e volta depois. */
const logado: CanActivateFn = (_r, state) => {
  const auth = inject(AuthService);
  return auth.logado ? true : inject(Router).createUrlTree(['/login'], { queryParams: { voltar: state.url } });
};

/** Tela de usuarios: so administrador. */
const admin: CanActivateFn = () => (inject(AuthService).admin ? true : inject(Router).createUrlTree(['/painel']));

export const routes: Routes = [
  { path: 'black-friday', component: VitrinePage, title: 'Black Friday Magica | MagicPass' },
  { path: 'login', component: LoginPage, title: 'Entrar | MagicPass' },
  {
    path: '',
    component: Shell,
    canActivate: [logado],
    children: [
      { path: 'painel', component: PainelPage, title: 'Painel | MagicPass' },
      { path: 'vendas', component: VendasPage, title: 'Vendas | MagicPass' },
      { path: 'vendas/nova', component: VendaFormPage, title: 'Nova venda | MagicPass' },
      { path: 'vendas/:id', component: VendaFormPage, title: 'Alterar venda | MagicPass' },
      { path: 'clientes', component: CrudPage, data: { config: CLIENTES }, title: 'Clientes | MagicPass' },
      { path: 'parceiros', component: CrudPage, data: { config: PARCEIROS }, title: 'Parceiros | MagicPass' },
      { path: 'parques', component: CrudPage, data: { config: PARQUES }, title: 'Parques | MagicPass' },
      { path: 'produtos', component: CrudPage, data: { config: PRODUTOS }, title: 'Produtos | MagicPass' },
      { path: 'promocoes', component: CrudPage, data: { config: PROMOCOES }, title: 'Promocoes | MagicPass' },
      { path: 'usuarios', component: CrudPage, data: { config: USUARIOS }, canActivate: [admin], title: 'Usuarios | MagicPass' },
      { path: '', pathMatch: 'full', redirectTo: 'painel' },
    ],
  },
  { path: '**', redirectTo: 'black-friday' },
];
