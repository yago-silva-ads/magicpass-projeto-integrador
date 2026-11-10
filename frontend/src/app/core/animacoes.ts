import { Directive, effect, ElementRef, inject, input, OnDestroy, OnInit } from '@angular/core';

const reduzido = () => typeof matchMedia !== 'undefined' && matchMedia('(prefers-reduced-motion: reduce)').matches;

/**
 * Revela o elemento com fade + subida quando ele entra na tela.
 * Uso: <div revelar [atraso]="80"> (atraso em ms para efeito cascata).
 */
@Directive({ selector: '[revelar]', host: { class: 'revelar' } })
export class Revelar implements OnInit, OnDestroy {
  private el = inject(ElementRef<HTMLElement>);
  readonly atraso = input(0);
  private obs?: IntersectionObserver;

  ngOnInit() {
    const node = this.el.nativeElement as HTMLElement;
    node.style.setProperty('--atraso', `${this.atraso()}ms`);
    if (reduzido() || typeof IntersectionObserver === 'undefined') {
      node.classList.add('visivel');
      return;
    }
    this.obs = new IntersectionObserver(
      (entradas) => entradas.forEach((e) => {
        if (e.isIntersecting) {
          node.classList.add('visivel');
          this.obs?.disconnect();
        }
      }),
      { threshold: 0.12, rootMargin: '0px 0px -40px 0px' },
    );
    this.obs.observe(node);
  }

  ngOnDestroy() {
    this.obs?.disconnect();
  }
}

/** Anima um numero de 0 ate o valor (ex.: KPIs do painel). */
@Directive({ selector: '[contador]' })
export class Contador implements OnDestroy {
  private el = inject(ElementRef<HTMLElement>);
  readonly contador = input.required<number>();
  readonly formato = input<'moeda' | 'inteiro'>('inteiro');
  private raf = 0;
  private fmtMoeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

  constructor() {
    effect(() => this.animar(Number(this.contador()) || 0));
  }

  private escrever(v: number) {
    (this.el.nativeElement as HTMLElement).textContent =
      this.formato() === 'moeda' ? this.fmtMoeda.format(v) : Math.round(v).toLocaleString('pt-BR');
  }

  private animar(alvo: number) {
    cancelAnimationFrame(this.raf);
    if (reduzido()) return this.escrever(alvo);
    const inicio = performance.now();
    const dur = 900;
    const passo = (t: number) => {
      const p = Math.min(1, (t - inicio) / dur);
      const ease = 1 - Math.pow(1 - p, 3); // ease-out cubic
      this.escrever(alvo * ease);
      if (p < 1) this.raf = requestAnimationFrame(passo);
    };
    this.raf = requestAnimationFrame(passo);
  }

  ngOnDestroy() {
    cancelAnimationFrame(this.raf);
  }
}
