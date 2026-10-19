import { Injectable, signal } from '@angular/core';

export type ThemeMode = 'light' | 'deep-space';

export interface ThemeOption {
  id: ThemeMode;
  name: string;
  icon: string;
  previewBg: string;
}

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  public theme = signal<ThemeMode>(this.getInitialTheme());

  public readonly themeOptions: ThemeOption[] = [
    { id: 'light', name: 'Light', icon: '☀️', previewBg: 'bg-white' },
    { id: 'deep-space', name: 'Deep Space', icon: '🚀', previewBg: 'bg-slate-950' }
  ];

  constructor() {
    this.applyTheme(this.theme());
  }

  public setTheme(mode: ThemeMode): void {
    this.theme.set(mode);
    try {
      localStorage.setItem('priora-theme', mode);
    } catch {}
    this.applyTheme(mode);
  }

  public cycleTheme(): void {
    this.setTheme(this.theme() === 'light' ? 'deep-space' : 'light');
  }

  private getInitialTheme(): ThemeMode {
    try {
      const saved = localStorage.getItem('priora-theme');
      if (saved === 'light' || saved === 'deep-space') {
        return saved;
      }
    } catch {}
    return 'deep-space'; // Default to Deep Space
  }

  private applyTheme(mode: ThemeMode): void {
    const root = document.documentElement;
    root.setAttribute('data-theme', mode);

    if (mode === 'light') {
      root.classList.remove('dark');
      root.classList.remove('deep-space');
    } else {
      root.classList.add('dark');
      root.classList.add('deep-space');
    }
  }
}
