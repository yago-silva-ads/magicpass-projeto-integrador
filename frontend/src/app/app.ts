import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastService } from './core/api';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: `
    <router-outlet />
    <div class="toasts" aria-live="polite">
      @for (t of toast.toasts(); track t.id) {
        <div [class]="'toast ' + t.tipo" (click)="toast.fechar(t.id)">{{ t.texto }}</div>
      }
    </div>
  `,
})
export class App {
  readonly toast = inject(ToastService);
}
