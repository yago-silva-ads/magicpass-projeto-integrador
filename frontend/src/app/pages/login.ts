import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService, lerErro } from '../core/api';
import { Estrela, Icone } from '../core/icone';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink, Estrela, Icone],
  template: `
    <div class="login">
      <section class="login-foto">
        <img src="img/main-street.jpg" alt="Main Street, U.S.A. no Magic Kingdom" />
        <div class="login-foto-texto">
          <span class="chip-ouro">Black Friday Magica 2026</span>
          <h2>Venda a viagem dos sonhos<br />com a magia dos parques.</h2>
          <p>Ingressos, pacotes com aereo e hotel, cupons e descontos para familias, tudo em um so lugar.</p>
        </div>
      </section>
      <section class="login-lado">
        <form class="login-cartao" (ngSubmit)="entrar()" novalidate>
          <div class="logo-grande"><app-estrela [tamanho]="40" /> MagicPass <span>Viagens</span></div>
          <p class="sub">Area do consultor - acesso restrito</p>
          <label class="campo">
            <span>Login</span>
            <input name="login" [(ngModel)]="login" autocomplete="username" autofocus />
          </label>
          <label class="campo">
            <span>Senha</span>
            <input name="senha" type="password" [(ngModel)]="senha" autocomplete="current-password" />
          </label>
          @if (erro()) { <p class="alerta-erro tremer">{{ erro() }}</p> }
          <button class="btn primario largo" [disabled]="entrando()">
            {{ entrando() ? 'Entrando...' : 'Entrar' }} @if (!entrando()) { <app-icone nome="seta" [tamanho]="16" /> }
          </button>
          <a class="link-black" routerLink="/black-friday"><app-icone nome="vitrine" [tamanho]="16" /> Ver ofertas Black Friday</a>
          <p class="dica-login"><app-icone nome="escudo" [tamanho]="14" /> Usuarios sao cadastrados pelo administrador.</p>
        </form>
      </section>
    </div>
  `,
})
export class LoginPage {
  private auth = inject(AuthService);
  private router = inject(Router);
  private destino = inject(ActivatedRoute).snapshot.queryParamMap.get('voltar') ?? '/painel';
  login = '';
  senha = '';
  readonly erro = signal('');
  readonly entrando = signal(false);

  async entrar() {
    this.erro.set('');
    if (!this.login.trim() || !this.senha) {
      this.erro.set('Informe login e senha');
      return;
    }
    this.entrando.set(true);
    try {
      await this.auth.entrar(this.login.trim(), this.senha);
      this.router.navigateByUrl(this.destino.startsWith('/') ? this.destino : '/painel');
    } catch (e) {
      this.erro.set(lerErro(e).mensagem);
    } finally {
      this.entrando.set(false);
    }
  }
}
