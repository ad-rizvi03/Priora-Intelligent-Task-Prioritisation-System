import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';

/**
 * Standalone Login Modal Component.
 * Supports explicit username/password authentication against Spring Security.
 */
@Component({
  selector: 'app-login-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="fixed inset-0 bg-slate-950/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div class="theme-modal border theme-border w-full max-w-md rounded-2xl p-6 shadow-2xl space-y-5">
        
        <!-- Modal Header -->
        <div class="flex items-center justify-between border-b theme-border-subtle pb-3">
          <div class="flex items-center gap-2">
            <div class="w-7 h-7 rounded bg-indigo-600 flex items-center justify-center text-white font-bold text-xs">
              G
            </div>
            <h2 class="text-base font-bold theme-text-heading">Sign In to Gravity Canvas</h2>
          </div>
          <button (click)="close.emit()" class="theme-text-muted hover:text-white font-bold text-sm cursor-pointer">✕</button>
        </div>

        <!-- Error Banner -->
        <div *ngIf="errorMessage" class="bg-rose-500/15 border border-rose-500/30 text-rose-400 px-3.5 py-2 rounded-lg text-xs font-medium">
          {{ errorMessage }}
        </div>

        <!-- Form -->
        <form (ngSubmit)="onLogin()" class="space-y-4">
          
          <div>
            <label class="block text-xs font-semibold theme-text-heading mb-1">Username *</label>
            <input type="text" [(ngModel)]="username" name="username" required
                   placeholder="e.g. manager or developer"
                   class="w-full theme-input rounded-lg px-3 py-2 text-xs focus:outline-none">
          </div>

          <div>
            <label class="block text-xs font-semibold theme-text-heading mb-1">Password *</label>
            <input type="password" [(ngModel)]="password" name="password" required
                   placeholder="••••••••"
                   class="w-full theme-input rounded-lg px-3 py-2 text-xs focus:outline-none">
          </div>

          <!-- Quick Test Credentials Chips -->
          <div class="theme-inner rounded-lg p-2.5 border theme-border-subtle text-[11px] space-y-1.5">
            <span class="theme-text-muted font-semibold block text-[10px] uppercase tracking-wider">Quick Fill Test Accounts:</span>
            <div class="flex gap-2">
              <button type="button" (click)="fillCreds('manager', 'password123')"
                      class="px-2.5 py-1 theme-btn-subtle text-indigo-400 border theme-border-subtle rounded font-medium text-xs cursor-pointer shadow-2xs">
                👑 manager / password123
              </button>
              <button type="button" (click)="fillCreds('developer', 'password123')"
                      class="px-2.5 py-1 theme-btn-subtle text-emerald-400 border theme-border-subtle rounded font-medium text-xs cursor-pointer shadow-2xs">
                💻 developer / password123
              </button>
            </div>
          </div>

          <!-- Submit Buttons -->
          <div class="flex items-center justify-end gap-2 pt-3 border-t theme-border-subtle">
            <button type="button" (click)="close.emit()" class="px-3.5 py-1.5 theme-btn-subtle rounded-lg text-xs font-medium cursor-pointer">
              Cancel
            </button>
            <button type="submit" [disabled]="!username || !password || loading"
                    class="px-4 py-1.5 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-lg text-xs font-medium shadow-sm flex items-center gap-1.5 cursor-pointer">
              <span *ngIf="loading" class="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
              Sign In
            </button>
          </div>

        </form>

      </div>
    </div>
  `
})
export class LoginModalComponent {
  @Output() close = new EventEmitter<void>();

  username = '';
  password = '';
  loading = false;
  errorMessage: string | null = null;

  constructor(
    private authService: AuthService,
    private taskService: TaskService
  ) {}

  fillCreds(user: string, pass: string): void {
    this.username = user;
    this.password = pass;
  }

  onLogin(): void {
    if (!this.username || !this.password) return;
    this.loading = true;
    this.errorMessage = null;

    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.loading = false;
        this.taskService.loadTasks(); // Reload tasks for newly authenticated user
        this.close.emit();
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Invalid username or password. Please try again.';
      }
    });
  }
}
