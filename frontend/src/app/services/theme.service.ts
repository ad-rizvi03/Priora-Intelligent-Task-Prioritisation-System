import { Injectable, signal } from '@angular/core';

export type ThemeMode = 'light' | 'midnight' | 'deep-space';

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
    { id: 'midnight', name: 'Midnight', icon: '🌙', previewBg: 'bg-slate-900' },
    { id: 'deep-space', name: 'Deep Space', icon: '🚀', previewBg: 'bg-black' }
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
    const current = this.theme();
    if (current === 'light') this.setTheme('midnight');
    else if (current === 'midnight') this.setTheme('deep-space');
    else this.setTheme('light');
  }

  private getInitialTheme(): ThemeMode {
    try {
      const saved = localStorage.getItem('priora-theme') as ThemeMode;
      if (saved === 'light' || saved === 'midnight' || saved === 'deep-space') {
        return saved;
      }
    } catch {}
    return 'midnight'; // Default to sleek Midnight dark
  }

  private applyTheme(mode: ThemeMode): void {
    const root = document.documentElement;
    root.setAttribute('data-theme', mode);

    if (mode === 'light') {
      root.classList.remove('dark');
      root.classList.remove('deep-space');
    } else if (mode === 'midnight') {
      root.classList.add('dark');
      root.classList.remove('deep-space');
    } else {
      root.classList.add('dark');
      root.classList.add('deep-space');
    }
  }
}
