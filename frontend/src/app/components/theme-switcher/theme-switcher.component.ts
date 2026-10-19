import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ThemeService, ThemeMode } from '../../services/theme.service';

/**
 * Enterprise Dynamic Theme Switcher Component.
 * Provides instant 1-click switching between Light, Midnight Dark, and Deep Space Cosmos themes.
 */
@Component({
  selector: 'app-theme-switcher',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="flex items-center gap-1 p-1 rounded-xl border transition-all text-xs"
         [ngClass]="getContainerClasses()">
      
      <button *ngFor="let opt of themeService.themeOptions"
              (click)="themeService.setTheme(opt.id)"
              [title]="'Switch to ' + opt.name + ' Theme'"
              [class]="getButtonClasses(opt.id)"
              class="px-2.5 py-1 rounded-lg font-semibold flex items-center gap-1.5 transition-all cursor-pointer">
        <span class="text-xs">{{ opt.icon }}</span>
        <span class="hidden sm:inline text-[11px]">{{ opt.name }}</span>
      </button>

    </div>
  `
})
export class ThemeSwitcherComponent {
  constructor(public themeService: ThemeService) {}

  getContainerClasses(): string {
    const current = this.themeService.theme();
    if (current === 'deep-space') {
      return 'bg-slate-950/90 border-slate-800 shadow-inner';
    }
    if (current === 'midnight') {
      return 'bg-slate-800/90 border-slate-700/80 shadow-inner';
    }
    return 'bg-slate-100 border-slate-200/80 shadow-2xs';
  }

  getButtonClasses(mode: ThemeMode): string {
    const active = this.themeService.theme() === mode;
    if (!active) {
      return 'text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-white';
    }

    if (mode === 'deep-space') {
      return 'bg-gradient-to-r from-purple-600 to-indigo-600 text-white shadow-md shadow-purple-500/20 font-bold';
    }
    if (mode === 'midnight') {
      return 'bg-slate-900 text-indigo-400 border border-indigo-500/30 shadow-md font-bold';
    }
    return 'bg-white text-indigo-700 shadow-xs font-bold border border-slate-200';
  }
}
