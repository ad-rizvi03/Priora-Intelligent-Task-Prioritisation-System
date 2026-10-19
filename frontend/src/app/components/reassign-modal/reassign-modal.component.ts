import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Task } from '../../models/task.model';
import { User } from '../../models/user.model';
import { TaskService } from '../../services/task.service';

/**
 * Team Lead & Project Manager Reassign Task Modal Component.
 * Features Gravitational Burnout Circuit Breaker, Supernova Event Horizon prevention,
 * and intelligent alternative developer recommendations.
 */
@Component({
  selector: 'app-reassign-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="fixed inset-0 bg-slate-950/70 backdrop-blur-xs z-50 flex items-center justify-center p-4">
      <div class="theme-modal rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-5 border theme-border">
        
        <div class="flex items-center justify-between border-b theme-border-subtle pb-3">
          <div>
            <h2 class="text-base font-bold theme-text-heading tracking-tight">Reassign Task</h2>
            <p class="text-xs theme-text-muted font-normal">Task #{{ task.id }} - {{ task.title }}</p>
          </div>
          <button (click)="close.emit()" class="theme-text-muted hover:text-white text-lg font-bold cursor-pointer">
            &times;
          </button>
        </div>

        <form (ngSubmit)="onSubmit()" class="space-y-4">
          <div class="space-y-2">
            <label class="block text-xs font-semibold theme-text-heading">Select Assignee (Developer / Tester) *</label>
            <select [(ngModel)]="assigneeId" name="assigneeId" (change)="onAssigneeChange()"
                    class="w-full theme-input rounded-xl px-3.5 py-2 text-xs focus:outline-none font-medium">
              <option [ngValue]="null">-- Unassigned --</option>
              <option *ngFor="let user of getAssignableUsers()" [value]="user.id">
                {{ user.fullName }} ({{ user.role === 'ROLE_TESTER' ? 'Tester' : 'Developer' }} • {{ user.activeGravityLoad || 0 }} pts | {{ user.capacityPercentage || 0 }}% load {{ user.burnoutStatus === 'SUPERNOVA_LOCKED' ? '🔴 SUPERNOVA' : user.burnoutStatus === 'HEAVY' ? '🟡 HEAVY' : '🟢 NORMAL' }})
              </option>
            </select>

            <!-- Circuit Breaker Warning & Alternative Recommendation Banner -->
            <div *ngIf="isAssigneeOverloaded()" class="p-3.5 bg-rose-500/15 border border-rose-500/30 rounded-xl space-y-2.5 text-xs">
              <div class="flex items-start gap-2 text-rose-400 font-bold">
                <span class="text-base shrink-0">🛡️</span>
                <div>
                  <div class="flex items-center gap-2">
                    <span>Gravitational Circuit Breaker Active</span>
                    <span class="bg-rose-600 text-white text-[10px] uppercase font-extrabold px-2 py-0.5 rounded-full">
                      Supernova ({{ getSelectedAssignee()?.capacityPercentage }}%)
                    </span>
                  </div>
                  <p class="text-[11px] text-rose-300 font-normal mt-0.5">
                    <strong>{{ getSelectedAssignee()?.fullName }}</strong> is overloaded with <strong>{{ getSelectedAssignee()?.activeGravityLoad }} active gravity points</strong>. Reassigning this task will risk sprint failure.
                  </p>
                </div>
              </div>

              <!-- Recommended Alternative Quick-Assign Action -->
              <div *ngIf="getRecommendedAlternative() as alt" class="theme-card border theme-border-subtle rounded-lg p-2.5 flex items-center justify-between">
                <div class="text-[11px]">
                  <span class="theme-text-muted font-medium">Recommended Alternative:</span>
                  <span class="font-bold theme-text-heading ml-1.5">{{ alt.fullName }}</span>
                  <span class="text-emerald-400 font-semibold ml-1">({{ alt.activeGravityLoad || 0 }} pts • {{ alt.capacityPercentage || 0 }}% load)</span>
                </div>
                <button type="button" (click)="selectAlternative(alt)" 
                        class="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-[10px] rounded-md transition-all shadow-xs cursor-pointer">
                  Assign to {{ alt.fullName.split(' ')[0] }}
                </button>
              </div>

              <!-- Emergency Manager Override Toggle -->
              <label class="flex items-center gap-2 text-[11px] text-rose-400 font-bold cursor-pointer pt-1 border-t border-rose-500/20">
                <input type="checkbox" [(ngModel)]="overrideCircuitBreaker" name="overrideCircuitBreaker" class="rounded border-rose-300 text-rose-600 focus:ring-rose-500 w-3.5 h-3.5">
                <span>Emergency Manager Override (Bypass Circuit Breaker)</span>
              </label>
            </div>
          </div>

          <div class="flex items-center justify-end gap-2 pt-3 border-t theme-border-subtle">
            <button type="button" (click)="close.emit()"
                    class="px-4 py-2 theme-btn-subtle rounded-xl text-xs font-medium transition-all cursor-pointer">
              Cancel
            </button>
            <button type="submit" [disabled]="loading || (isAssigneeOverloaded() && !overrideCircuitBreaker)"
                    class="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-xl text-xs font-semibold shadow-sm transition-all flex items-center gap-2 cursor-pointer">
              <span *ngIf="loading" class="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
              <span *ngIf="isAssigneeOverloaded() && !overrideCircuitBreaker">Blocked by Circuit Breaker</span>
              <span *ngIf="!isAssigneeOverloaded() || overrideCircuitBreaker">Confirm Reassignment</span>
            </button>
          </div>
        </form>

      </div>
    </div>
  `
})
export class ReassignModalComponent implements OnInit {
  @Input({ required: true }) task!: Task;
  @Output() close = new EventEmitter<void>();

  assigneeId: number | null = null;
  overrideCircuitBreaker = false;
  loading = false;

  constructor(public taskService: TaskService) {}

  ngOnInit(): void {
    this.taskService.loadUsers();
    if (this.task && this.task.assignee) {
      this.assigneeId = this.task.assignee.id;
    }
  }

  getAssignableUsers(): User[] {
    return this.taskService.users().filter(u => u.role === 'ROLE_TEAM_MEMBER' || u.role === 'ROLE_TESTER');
  }

  onAssigneeChange(): void {
    this.overrideCircuitBreaker = false;
  }

  getSelectedAssignee(): User | undefined {
    if (!this.assigneeId) return undefined;
    return this.taskService.users().find(u => u.id === Number(this.assigneeId));
  }

  isAssigneeOverloaded(): boolean {
    const user = this.getSelectedAssignee();
    return user?.burnoutStatus === 'SUPERNOVA_LOCKED';
  }

  getRecommendedAlternative(): User | undefined {
    const currentId = Number(this.assigneeId);
    return this.getAssignableUsers()
      .filter(u => u.id !== currentId && u.burnoutStatus !== 'SUPERNOVA_LOCKED')
      .sort((a, b) => (a.activeGravityLoad || 0) - (b.activeGravityLoad || 0))[0];
  }

  selectAlternative(user: User): void {
    this.assigneeId = user.id;
    this.overrideCircuitBreaker = false;
  }

  onSubmit(): void {
    this.loading = true;
    const targetId = this.assigneeId ? Number(this.assigneeId) : null;
    this.taskService.reassignTask(this.task.id, targetId, this.overrideCircuitBreaker).subscribe({
      next: () => {
        this.loading = false;
        this.close.emit();
      },
      error: () => this.loading = false
    });
  }
}
