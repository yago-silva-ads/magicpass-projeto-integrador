import { HttpClient, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, firstValueFrom, throwError } from 'rxjs';
import { Entidade, ErroApi, Sessao } from './modelos';

export const API = '/api';

/** Converte qualquer erro HTTP em {status, mensagem, campos}. */
export function lerErro(e: unknown): ErroApi {
  if (e instanceof HttpErrorResponse) {
    if (e.status === 0) return { status: 0, mensagem: 'Servidor indisponivel. Verifique se o backend (porta 8080) esta rodando.' };
    const corpo = e.error ?? {};
    return { status: e.status, mensagem: corpo.mensagem ?? `Erro ${e.status}`, campos: corpo.campos ?? null };
  }
  return { status: -1, mensagem: (e as Error)?.message ?? 'Erro inesperado' };
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);

  listar<T = Entidade>(recurso: string) {
    return firstValueFrom(this.http.get<T[]>(`${API}/${recurso}`));
  }
  buscar<T = Entidade>(recurso: string, id: number) {
    return firstValueFrom(this.http.get<T>(`${API}/${recurso}/${id}`));
  }
  criar<T = Entidade>(recurso: string, dados: unknown) {
    return firstValueFrom(this.http.post<T>(`${API}/${recurso}`, dados));
  }
  atualizar<T = Entidade>(recurso: string, id: number, dados: unknown) {
    return firstValueFrom(this.http.put<T>(`${API}/${recurso}/${id}`, dados));
  }
  excluir(recurso: string, id: number) {
    return firstValueFrom(this.http.delete<void>(`${API}/${recurso}/${id}`));
  }
  get<T>(caminho: string) {
    return firstValueFrom(this.http.get<T>(`${API}/${caminho}`));
  }
  post<T>(caminho: string, dados: unknown) {
    return firstValueFrom(this.http.post<T>(`${API}/${caminho}`, dados));
  }
  put<T>(caminho: string, dados: unknown) {
    return firstValueFrom(this.http.put<T>(`${API}/${caminho}`, dados));
  }
}

const CHAVE = 'magicpass.sessao';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  readonly sessao = signal<Sessao | null>(this.carregar());

  private carregar(): Sessao | null {
    try {
      return JSON.parse(sessionStorage.getItem(CHAVE) ?? 'null');
    } catch {
      return null;
    }
  }

  get logado() { return this.sessao() !== null; }
  get admin() { return this.sessao()?.perfil === 'ADMIN'; }
  get token() { return this.sessao()?.token ?? null; }

  async entrar(login: string, senha: string) {
    const s = await firstValueFrom(this.http.post<Sessao>(`${API}/auth/login`, { login, senha }));
    sessionStorage.setItem(CHAVE, JSON.stringify(s));
    this.sessao.set(s);
    return s;
  }

  async sair(chamarApi = true) {
    if (chamarApi && this.token) {
      try { await firstValueFrom(this.http.post(`${API}/auth/logout`, {})); } catch { /* sessao ja expirada */ }
    }
    sessionStorage.removeItem(CHAVE);
    this.sessao.set(null);
    this.router.navigate(['/login']);
  }
}

/** Anexa o token e, se a sessao expirar (401), volta para o login. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token;
  const r = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(r).pipe(
    catchError((e: HttpErrorResponse) => {
      if (e.status === 401 && !req.url.endsWith('/auth/login') && !req.url.endsWith('/auth/logout')) {
        auth.sair(false);
      }
      return throwError(() => e);
    })
  );
};

export interface Toast { id: number; tipo: 'ok' | 'erro' | 'info'; texto: string; }

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);
  private seq = 0;

  mostrar(texto: string, tipo: Toast['tipo'] = 'ok') {
    const t = { id: ++this.seq, tipo, texto };
    this.toasts.update((l) => [...l, t]);
    setTimeout(() => this.fechar(t.id), tipo === 'erro' ? 6000 : 3500);
  }
  ok(texto: string) { this.mostrar(texto, 'ok'); }
  erro(e: unknown) { this.mostrar(typeof e === 'string' ? e : lerErro(e).mensagem, 'erro'); }
  fechar(id: number) { this.toasts.update((l) => l.filter((t) => t.id !== id)); }
}
