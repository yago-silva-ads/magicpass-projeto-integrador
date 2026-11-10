import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Icone } from '../../core/icone';
import { ActivatedRoute } from '@angular/router';
import { ApiService, AuthService, lerErro, ToastService } from '../../core/api';
import { Entidade } from '../../core/modelos';
import { Campo, CrudConfig } from './crud-config';

/**
 * Tela de CRUD generica: Listar, Cadastrar, Alterar e Excluir.
 * Cada cadastro so define sua configuracao (campos, colunas, validacoes) em crud-config.ts.
 */
@Component({
  selector: 'app-crud-page',
  imports: [FormsModule, Icone],
  templateUrl: './crud-page.html',
})
export class CrudPage implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  readonly auth = inject(AuthService);
  readonly cfg: CrudConfig = inject(ActivatedRoute).snapshot.data['config'];

  readonly registros = signal<Entidade[]>([]);
  readonly carregando = signal(true);
  readonly busca = signal('');
  readonly opcoesRef = signal<Partial<Record<string, { valor: number; rotulo: string }[]>>>({});

  // formulario
  readonly aberto = signal(false);
  readonly salvando = signal(false);
  readonly editandoId = signal<number | null>(null);
  modelo: Record<string, any> = {};
  readonly erros = signal<Record<string, string>>({});
  readonly erroGeral = signal('');

  // exclusao
  readonly excluindo = signal<Entidade | null>(null);

  readonly podeEditar = computed(() => !this.cfg.somenteAdminEdita || this.auth.admin);

  readonly filtrados = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    if (!termo) return this.registros();
    return this.registros().filter((r) =>
      this.cfg.colunas.some((c) => String(c.valor(r) ?? '').toLowerCase().includes(termo)));
  });

  ngOnInit() {
    this.carregar();
  }

  async carregar() {
    this.carregando.set(true);
    try {
      this.registros.set(await this.api.listar(this.cfg.recurso));
    } catch (e) {
      this.toast.erro(e);
    } finally {
      this.carregando.set(false);
    }
  }

  private async carregarRefs() {
    const refs = this.cfg.campos.filter((c) => c.tipo === 'ref' && c.ref);
    const mapa: Record<string, { valor: number; rotulo: string }[]> = {};
    for (const c of refs) {
      try {
        const lista = await this.api.listar(c.ref!.recurso);
        mapa[c.nome] = lista.map((e) => ({ valor: e.id!, rotulo: c.ref!.rotulo(e) }));
      } catch (e) {
        this.toast.erro(e);
      }
    }
    this.opcoesRef.set(mapa);
  }

  visivel(c: Campo) {
    return !c.visivel || c.visivel(this.modelo);
  }

  async abrirNovo() {
    this.modelo = this.cfg.novo();
    this.editandoId.set(null);
    this.limparErros();
    await this.carregarRefs();
    this.aberto.set(true);
  }

  async abrirEdicao(r: Entidade) {
    this.limparErros();
    await this.carregarRefs();
    try {
      const atual = await this.api.buscar(this.cfg.recurso, r.id!); // sempre busca a versao mais recente
      const m: Record<string, any> = { ...this.cfg.novo() };
      for (const c of this.cfg.campos) {
        const v = atual[c.nome];
        if (c.tipo === 'ref') m[c.nome] = v?.id ?? null;
        else if (c.tipo === 'datahora') m[c.nome] = v ? String(v).slice(0, 16) : '';
        else if (c.tipo === 'senha') m[c.nome] = '';
        else m[c.nome] = v ?? (c.tipo === 'check' ? false : '');
      }
      this.modelo = m;
      this.editandoId.set(atual.id!);
      this.aberto.set(true);
    } catch (e) {
      this.toast.erro(e);
      this.carregar(); // registro pode ter sido apagado por outro usuario
    }
  }

  fechar() {
    this.aberto.set(false);
  }

  private limparErros() {
    this.erros.set({});
    this.erroGeral.set('');
  }

  private validar(): boolean {
    const erros: Record<string, string> = {};
    const editando = this.editandoId() !== null;
    for (const c of this.cfg.campos) {
      if (!this.visivel(c)) continue;
      const v = this.modelo[c.nome];
      const vazio = v === null || v === undefined || (typeof v === 'string' && v.trim() === '');
      if (c.obrigatorio && vazio && c.tipo !== 'check') {
        erros[c.nome] = `${c.rotulo} e obrigatorio`;
        continue;
      }
      if (!vazio || c.tipo === 'senha') {
        const msg = c.validar?.(v, this.modelo, editando);
        if (msg) erros[c.nome] = msg;
      }
    }
    this.erros.set(erros);
    return Object.keys(erros).length === 0;
  }

  /** Monta o JSON para a API (referencias viram {id}, vazios viram null). */
  private payload() {
    const p: Record<string, any> = {};
    for (const c of this.cfg.campos) {
      let v = this.modelo[c.nome];
      if (!this.visivel(c)) v = c.tipo === 'check' ? false : c.tipo === 'numero' ? 0 : null;
      if (typeof v === 'string') v = v.trim();
      if (v === '') v = null;
      if (c.tipo === 'ref') v = v ? { id: Number(v) } : null;
      if ((c.tipo === 'numero' || c.tipo === 'dinheiro') && v !== null) v = Number(v);
      if (c.nome === 'cupom' && v) v = String(v).toUpperCase();
      p[c.nome] = v;
    }
    return p;
  }

  async salvar() {
    this.erroGeral.set('');
    if (!this.validar()) {
      this.erroGeral.set('Corrija os campos destacados.');
      return;
    }
    this.salvando.set(true);
    try {
      const id = this.editandoId();
      if (id) {
        await this.api.atualizar(this.cfg.recurso, id, this.payload());
        this.toast.ok(`${this.cap(this.cfg.singular)} alterado(a) com sucesso`);
      } else {
        await this.api.criar(this.cfg.recurso, this.payload());
        this.toast.ok(`${this.cap(this.cfg.singular)} cadastrado(a) com sucesso`);
      }
      this.aberto.set(false);
      await this.carregar();
    } catch (e) {
      const erro = lerErro(e);
      this.erros.set(erro.campos ?? {});
      this.erroGeral.set(erro.mensagem);
      if (erro.status === 404) this.carregar();
    } finally {
      this.salvando.set(false);
    }
  }

  confirmarExclusao(r: Entidade) {
    this.excluindo.set(r);
  }

  async excluir() {
    const r = this.excluindo();
    if (!r?.id) return;
    try {
      await this.api.excluir(this.cfg.recurso, r.id);
      this.toast.ok(`${this.cap(this.cfg.singular)} excluido(a)`);
    } catch (e) {
      this.toast.erro(e);
    } finally {
      this.excluindo.set(null);
      this.carregar();
    }
  }

  tipoInput(c: Campo) {
    return ({ numero: 'number', dinheiro: 'number', email: 'email', data: 'date', datahora: 'datetime-local', senha: 'password' } as any)[c.tipo] ?? 'text';
  }

  cap(s: string) {
    return s.charAt(0).toUpperCase() + s.slice(1);
  }
}
