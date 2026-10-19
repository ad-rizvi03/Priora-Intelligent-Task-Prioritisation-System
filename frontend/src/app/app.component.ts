import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { HeaderComponent } from './components/header/header.component';
import { CreateTaskModalComponent } from './components/create-task-modal/create-task-modal.component';
import { EditDeadlineModalComponent } from './components/edit-deadline-modal/edit-deadline-modal.component';
import { ReassignModalComponent } from './components/reassign-modal/reassign-modal.component';
import { AuthService } from './services/auth.service';
import { TaskService } from './services/task.service';
import { ThemeService } from './services/theme.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    HeaderComponent,
    CreateTaskModalComponent,
    EditDeadlineModalComponent,
    ReassignModalComponent
  ],
  template: `
    <div style="background-color: var(--bg-app); color: var(--text-body);" 
         class="min-h-screen flex flex-col font-sans transition-colors duration-200">
      
      <!-- Top Corporate Navigation Header -->
      <app-header (openCreateModal)="showCreateModal = true"></app-header>

      <!-- Main Router Outlet (Renders /login, /canvas, /settings, /profile, /users) -->
      <main class="flex-1">
        <router-outlet></router-outlet>
      </main>

      <!-- Footer Bar -->
      <footer *ngIf="authService.isAuthenticated()" 
              style="background-color: var(--bg-card); border-color: var(--border-primary); color: var(--text-muted);"
              class="border-t py-3 text-center text-xs transition-colors duration-200">
        <div class="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between px-6 gap-2">
          <span>Priora Enterprise &bull; Self-Prioritizing Task Board powered by Mathematical Gravity</span>
          <span style="color: var(--text-muted);">PostgreSQL &bull; Spring Boot 3 &bull; Angular 16</span>
        </div>
      </footer>

      <!-- Global Modals Wired directly through TaskService Signals -->
      <app-create-task-modal *ngIf="showCreateModal" (close)="showCreateModal = false"></app-create-task-modal>
      <app-edit-deadline-modal *ngIf="taskService.selectedTaskForDeadline()" [task]="taskService.selectedTaskForDeadline()!" (close)="taskService.closeDeadlineModal()"></app-edit-deadline-modal>
      <app-reassign-modal *ngIf="taskService.selectedTaskForReassign()" [task]="taskService.selectedTaskForReassign()!" (close)="taskService.closeReassignModal()"></app-reassign-modal>

    </div>
  `
})
export class AppComponent {
  showCreateModal = false;

  constructor(
    public authService: AuthService,
    public taskService: TaskService,
    public themeService: ThemeService,
    private router: Router
  ) {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
    }
  }
}
