import { Injectable, effect, signal } from '@angular/core';

export type Tema = 'claro' | 'escuro';
const CHAVE = 'estoque.tema';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly tema = signal<Tema>(this.inicial());

  constructor() {
    effect(() => {
      const t = this.tema();
      document.documentElement.setAttribute('data-tema', t);
      localStorage.setItem(CHAVE, t);
    });
  }

  alternar(): void {
    this.tema.update((t) => (t === 'claro' ? 'escuro' : 'claro'));
  }

  private inicial(): Tema {
    const salvo = localStorage.getItem(CHAVE);
    if (salvo === 'claro' || salvo === 'escuro') return salvo;
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'escuro' : 'claro';
  }
}
