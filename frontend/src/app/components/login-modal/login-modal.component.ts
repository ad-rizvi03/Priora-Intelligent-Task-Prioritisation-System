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
    <div class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div class="bg-white border border-slate-200 w-full max-w-md rounded-2xl p-6 shadow-2xl space-y-5">
        
        <!-- Modal Header -->
        <div class="flex items-center justify-between border-b border-slate-100 pb-3">
          <div class="flex items-center gap-2">
            <div class="w-7 h-7 rounded bg-indigo-600 flex items-center justify-center text-white font-bold text-xs">
              G
            </div>
            <h2 class="text-base font-bold text-slate-900">Sign In to Gravity Canvas</h2>
          </div>
          <button (click)="close.emit()" class="text-slate-400 hover:text-slate-700 font-bold text-sm">✕</button>
        </div>

        <!-- Error Banner -->
        <div *ngIf="errorMessage" class="bg-rose-50 border border-rose-200 text-rose-700 px-3.5 py-2 rounded-lg text-xs font-medium">
          {{ errorMessage }}
        </div>

        <!-- Form -->
        <form (ngSubmit)="onLogin()" class="space-y-4">
          
          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1">Username *</label>
            <input type="text" [(ngModel)]="username" name="username" required
                   placeholder="e.g. manager or developer"
                   class="w-full bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 text-xs text-slate-900 focus:outline-none focus:border-indigo-600">
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1">Password *</label>
            <input type="password" [(ngModel)]="password" name="password" required
                   placeholder="••••••••"
                   class="w-full bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 text-xs text-slate-900 focus:outline-none focus:border-indigo-600">
          </div>

          <!-- Quick Test Credentials Chips -->
          <div class="bg-slate-50 rounded-lg p-2.5 border border-slate-200/80 text-[11px] space-y-1.5">
            <span class="text-slate-500 font-semibold block text-[10px] uppercase tracking-wider">Quick Fill Test Accounts:</span>
            <div class="flex gap-2">
              <button type="button" (click)="fillCreds('manager', 'password123')"
                      class="px-2.5 py-1 bg-white hover:bg-slate-100 text-indigo-700 border border-slate-200 rounded font-medium text-xs shadow-2xs">
                👑 manager / password123
              </button>
              <button type="button" (click)="fillCreds('developer', 'password123')"
                      class="px-2.5 py-1 bg-white hover:bg-slate-100 text-emerald-700 border border-slate-200 rounded font-medium text-xs shadow-2xs">
                💻 developer / password123
              </button>
            </div>
          </div>

          <!-- Submit Buttons -->
          <div class="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
            <button type="button" (click)="close.emit()" class="px-3.5 py-1.5 bg-slate-100 text-slate-700 rounded-lg text-xs font-medium hover:bg-slate-200">
              Cancel
            </button>
            <button type="submit" [disabled]="!username || !password || loading"
                    class="px-4 py-1.5 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-lg text-xs font-medium shadow-sm flex items-center gap-1.5">
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
