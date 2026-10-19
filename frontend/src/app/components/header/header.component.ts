import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { ThemeSwitcherComponent } from '../theme-switcher/theme-switcher.component';

/**
 * Corporate Minimalist Navigation Header Component.
 * Restricts Task Creation to Team Leads and Project Managers, features Ping Notification Alerts, and user profile links.
 */
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, ThemeSwitcherComponent],
  template: `
    <!-- Developer & Tester Ping Notification Stream Banner -->
    <div *ngIf="!authService.hasLeadPermissions() && taskService.developerNotifications().length > 0" 
         class="bg-amber-500 text-white px-6 py-2 text-xs font-bold flex items-center justify-between shadow-xs border-b border-amber-600 animate-pulse">
      <div class="flex items-center gap-2 max-w-7xl mx-auto w-full">
        <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
        </svg>
        <span>
          <strong>Priority Alert:</strong> Lead pinged {{ taskService.developerNotifications().length }} task(s) assigned to you: 
          <span *ngFor="let t of taskService.developerNotifications()" class="underline ml-1">#{{ t.id }} - {{ t.title }}</span>
        </span>
      </div>
    </div>

    <header *ngIf="authService.isAuthenticated()" 
            style="background-color: var(--bg-header); border-color: var(--border-primary);"
            class="border-b sticky top-0 z-30 px-6 py-3 shadow-2xs backdrop-blur-md transition-colors">
      <div class="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
        
        <!-- Brand & Main Navigation Links -->
        <div class="flex items-center gap-6">
          <a routerLink="/canvas" class="flex items-center gap-2.5 group">
            <div class="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white font-extrabold text-sm shadow-xs group-hover:bg-indigo-700 transition-all">
              P
            </div>
            <div>
              <h1 style="color: var(--text-heading);" class="text-lg font-bold tracking-tight leading-tight transition-colors">Priora</h1>
              <p style="color: var(--text-muted);" class="text-[10px] font-normal transition-colors">Enterprise Task Priority Engine</p>
            </div>
          </a>

          <!-- Router Navigation Tabs -->
          <nav style="background-color: var(--bg-card-subtle); border-color: var(--border-primary);"
               class="flex items-center gap-1 p-1 rounded-xl border text-xs transition-colors">
            <a routerLink="/canvas" routerLinkActive="active-nav-tab"
               style="color: var(--text-body);"
               class="px-3 py-1.5 rounded-lg font-medium transition-all hover:opacity-80">
              Canvas
            </a>

            <a *ngIf="authService.hasLeadPermissions()" routerLink="/settings" routerLinkActive="active-nav-tab"
               style="color: var(--text-body);"
               class="px-3 py-1.5 rounded-lg font-medium transition-all hover:opacity-80">
              Settings
            </a>

            <!-- Project Manager Only: User Provisioning -->
            <a *ngIf="authService.isProjectManager()" routerLink="/users" routerLinkActive="active-nav-tab"
               style="color: var(--text-body);"
               class="px-3 py-1.5 rounded-lg font-medium transition-all hover:opacity-80">
              Users
            </a>
          </nav>
        </div>

        <!-- Right Side User Profile & Actions -->
        <div class="flex items-center gap-2.5">
          
          <!-- Dynamic Theme Switcher -->
          <app-theme-switcher></app-theme-switcher>

          <!-- Team Lead / Project Manager Only: Create Task Button -->
          <button *ngIf="authService.hasLeadPermissions()" (click)="openCreateModal.emit()"
                  class="bg-indigo-600 hover:bg-indigo-700 text-white font-medium text-xs px-3.5 py-2 rounded-lg shadow-2xs transition-all flex items-center gap-1.5 cursor-pointer shrink-0">
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Create Task
          </button>

          <!-- User Profile Badge (Clicking opens /profile) -->
          <a routerLink="/profile" 
             title="View Profile & Change Password"
             style="background-color: var(--bg-card-subtle); border-color: var(--border-primary);"
             class="flex items-center gap-2 border px-3 py-1 rounded-lg transition-all group shrink-0">
            <div class="w-6 h-6 rounded-full bg-indigo-600 flex items-center justify-center font-bold text-white text-[10px]">
              {{ authService.currentUser()?.fullName?.charAt(0) || 'U' }}
            </div>
            <div class="text-left hidden sm:block">
              <div style="color: var(--text-heading);" class="text-xs font-semibold leading-tight transition-colors">
                {{ authService.currentUser()?.fullName }}
              </div>
              <div style="color: var(--text-muted);" class="text-[9px] font-semibold uppercase">
                {{ getRoleLabel(authService.currentUser()?.role) }}
              </div>
            </div>
          </a>

          <!-- Explicit Sign Out Button -->
          <button (click)="onSignOut()"
                  title="Sign Out"
                  style="background-color: var(--bg-card-subtle); border-color: var(--border-primary); color: var(--text-heading);"
                  class="px-3 py-1.5 rounded-lg text-xs font-medium border transition-all cursor-pointer hover:opacity-80 shrink-0">
            Sign Out
          </button>

        </div>

      </div>
    </header>
  `
})
export class HeaderComponent {
  @Output() openCreateModal = new EventEmitter<void>();

  constructor(
    public authService: AuthService,
    public taskService: TaskService,
    private router: Router
  ) {}

  getRoleLabel(role?: string): string {
    switch (role) {
      case 'ROLE_PROJECT_MANAGER': return 'Project Manager';
      case 'ROLE_TEAM_LEAD': return 'Team Lead';
      case 'ROLE_TESTER': return 'Tester';
      case 'ROLE_TEAM_MEMBER': return 'Developer';
      default: return role ? role.replace('ROLE_', '') : 'Developer';
    }
  }

  onSignOut(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
