import { HttpClient } from '@angular/common/http';
import { Injectable, Signal, inject, signal } from '@angular/core';
import { environment } from '../../environments/environment';

/**
 * Busca a imagem principal (GET item/{id}/imagem-principal) como blob pelo HttpClient
 * (passa pelo interceptor de auth) e expõe um object URL por item+versão.
 */
@Injectable({ providedIn: 'root' })
export class ImagemPrincipalService {
  private readonly http = inject(HttpClient);
  private readonly cache = new Map<string, ReturnType<typeof signal<string | null>>>();
  private readonly urls = new Map<string, string>();

  url(id: number, versao: string | null | undefined): Signal<string | null> {
    const chave = `${id}:${versao ?? ''}`;
    let s = this.cache.get(chave);
    if (!s) {
      s = signal<string | null>(null);
      this.cache.set(chave, s);
      this.revogarOutras(id, chave);
      const alvo = s;
      this.http
        .get(`${environment.apiUrl}/item/${id}/imagem-principal`, { params: { v: versao ?? '' }, responseType: 'blob' })
        .subscribe({
          next: (blob) => {
            const u = URL.createObjectURL(blob);
            this.urls.set(chave, u);
            alvo.set(u);
          },
          error: () => alvo.set(null),
        });
    }
    return s;
  }

  /** Descarta as imagens em cache do item (ex.: após trocar a principal). */
  invalidar(id: number): void {
    this.revogarOutras(id, null);
  }

  private revogarOutras(id: number, manter: string | null): void {
    for (const chave of [...this.cache.keys()]) {
      if (chave.startsWith(`${id}:`) && chave !== manter) {
        const u = this.urls.get(chave);
        if (u) URL.revokeObjectURL(u);
        this.urls.delete(chave);
        this.cache.delete(chave);
      }
    }
  }
}
