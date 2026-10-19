import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaskService } from '../../services/task.service';
import { SettingsService } from '../../services/settings.service';
import { AuthService } from '../../services/auth.service';
import { TaskEnvironment, CreateTaskRequest, Task } from '../../models/task.model';
import { User } from '../../models/user.model';

/**
 * Task Creation Modal Component (Project Manager & Team Lead Restricted).
 * Features Developer & Tester Assignee Selection, Past Date Disabling in Deadline Picker,
 * Team Lead Custom Dependency Weight option, and Parent Task Sequential Execution Ordering.
 */
@Component({
  selector: 'app-create-task-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="fixed inset-0 bg-slate-950/70 backdrop-blur-xs z-50 flex items-start justify-center p-4 overflow-y-auto">
      <div class="theme-modal rounded-2xl max-w-xl w-full p-6 shadow-2xl space-y-6 border theme-border my-auto">
        
        <!-- Modal Header -->
        <div class="flex items-center justify-between border-b theme-border-subtle pb-3">
          <div>
            <h2 class="text-lg font-bold theme-text-heading tracking-tight">Create New Task Card</h2>
            <p class="text-xs theme-text-muted font-normal mt-0.5">
              Assign an engineering or QA testing task evaluated by the Dynamic Gravity Engine.
            </p>
          </div>
          <button (click)="close.emit()" class="theme-text-muted hover:text-white text-lg font-bold cursor-pointer">
            &times;
          </button>
        </div>

        <!-- Task Form -->
        <form (ngSubmit)="onSubmit()" class="space-y-4">
          
          <!-- Title -->
          <div>
            <label class="block text-xs font-semibold theme-text-heading mb-1">Task Title *</label>
            <input type="text" [(ngModel)]="title" name="title" required
                   placeholder="e.g. Upgrade Spring Security & Log4j Dependencies"
                   class="w-full theme-input rounded-xl px-3.5 py-2 text-xs focus:outline-none">
          </div>

          <!-- Description -->
          <div>
            <label class="block text-xs font-semibold theme-text-heading mb-1">Description</label>
            <textarea [(ngModel)]="description" name="description" rows="2"
                      placeholder="Detail technical scope, impacts, or steps..."
                      class="w-full theme-input rounded-xl p-3 text-xs focus:outline-none"></textarea>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            
            <!-- Environment -->
            <div>
              <label class="block text-xs font-semibold theme-text-heading mb-1">Target Environment *</label>
              <select [(ngModel)]="environment" name="environment" required
                      class="w-full theme-input rounded-xl px-3.5 py-2 text-xs focus:outline-none">
                <option value="PRODUCTION">PRODUCTION (1.5x Multiplier)</option>
                <option value="STAGING">STAGING (1.1x Multiplier)</option>
                <option value="DEVELOPMENT">DEVELOPMENT (1.0x Multiplier)</option>
              </select>
            </div>

            <!-- Target Deadline (Past Dates Disabled via min attribute) -->
            <div>
              <label class="block text-xs font-semibold theme-text-heading mb-1">Target Deadline *</label>
              <input type="datetime-local" [(ngModel)]="deadlineInput" name="deadlineInput" required [min]="minDateTimeInput"
                     class="w-full theme-input rounded-xl px-3.5 py-2 text-xs focus:outline-none">
              <span class="text-[10px] theme-text-muted block mt-0.5">Past dates prior to current date are disabled</span>
            </div>

          </div>

          <!-- Assignee Selection (DEVELOPERS & TESTERS) with Gravitational Load Status -->
          <div class="space-y-2">
            <label class="block text-xs font-semibold theme-text-heading">Assignee (Developer / Tester) *</label>
            <select [(ngModel)]="assigneeId" name="assigneeId" (change)="onAssigneeChange()"
                    class="w-full theme-input rounded-xl px-3.5 py-2 text-xs focus:outline-none font-medium">
              <option [ngValue]="null">-- Select Assignee --</option>
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
                      Supernova Event Horizon ({{ getSelectedAssignee()?.capacityPercentage }}%)
                    </span>
                  </div>
                  <p class="text-[11px] text-rose-300 font-normal mt-0.5">
                    <strong>{{ getSelectedAssignee()?.fullName }}</strong> has reached maximum gravitational capacity with <strong>{{ getSelectedAssignee()?.activeGravityLoad }} active gravity points</strong>. Assigning further tasks will trigger delivery failure and engineer burnout.
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
                  Assign to {{ alt.fullName.split(' ')[0] }} Instead
                </button>
              </div>

              <!-- Emergency Manager Override Toggle -->
              <label class="flex items-center gap-2 text-[11px] text-rose-400 font-bold cursor-pointer pt-1 border-t border-rose-500/20">
                <input type="checkbox" [(ngModel)]="overrideCircuitBreaker" name="overrideCircuitBreaker" class="rounded border-rose-300 text-rose-600 focus:ring-rose-500 w-3.5 h-3.5">
                <span>Emergency Manager Override (Bypass Circuit Breaker at Delivery Risk)</span>
              </label>
            </div>
          </div>

          <!-- TEAM LEAD CUSTOM DEPENDENCY WEIGHT SELECTION -->
          <div *ngIf="settingsService.settings().enableCustomFactor" 
               class="bg-indigo-500/10 border border-indigo-500/30 p-3.5 rounded-xl text-xs space-y-1">
            <label class="flex items-center gap-2.5 cursor-pointer font-bold theme-text-heading">
              <input type="checkbox" [(ngModel)]="customFactorApplied" name="customFactorApplied"
                     class="rounded border-slate-300 text-indigo-600 focus:ring-indigo-500 w-4 h-4">
              <span>Apply Custom Dependency Weight: {{ settingsService.settings().customFactorName || 'Custom Weight' }}</span>
              <span class="bg-indigo-600 text-white font-extrabold text-[10px] px-2 py-0.5 rounded-full ml-auto">
                +{{ settingsService.settings().customFactorPoints || 0 }} pts
              </span>
            </label>
            <p class="text-[11px] theme-text-muted pl-6">
              Applies team lead custom dependency points directly to this task's Gravity Score.
            </p>
          </div>

          <!-- Parent Task Dependencies with Optional Sequential Ordering Option -->
          <div class="space-y-2">
            <div class="flex items-center justify-between">
              <label class="text-xs font-semibold theme-text-heading">
                Blocked By Parent Tasks (Dependencies)
              </label>

              <label class="flex items-center gap-1.5 text-[11px] text-indigo-400 font-semibold cursor-pointer">
                <input type="checkbox" [(ngModel)]="enableSequentialOrdering" name="enableSequentialOrdering"
                       class="rounded border-slate-300 text-indigo-600 focus:ring-indigo-500 w-3.5 h-3.5">
                Enable Sequential Execution Order (#1, #2...)
              </label>
            </div>
            
            <div class="theme-inner border theme-border-subtle rounded-xl p-3 max-h-36 overflow-y-auto space-y-2">
              <div *ngIf="taskService.tasks().length === 0" class="text-xs theme-text-muted italic">
                No existing tasks available to set as parent dependencies.
              </div>

              <div *ngFor="let parentTask of taskService.tasks()" 
                   class="flex items-center justify-between p-2 rounded-lg theme-card border theme-border-subtle transition-all text-xs">
                
                <label class="flex items-center gap-2.5 flex-1 cursor-pointer truncate">
                  <input type="checkbox"
                         [checked]="isParentSelected(parentTask.id)"
                         (change)="toggleParentDependency(parentTask.id)"
                         class="rounded border-slate-300 text-indigo-600 focus:ring-indigo-500 w-4 h-4">
                  
                  <div class="truncate">
                    <span class="font-bold theme-text-heading">#{{ parentTask.id }}</span> - 
                    <span class="theme-text-body font-medium">{{ parentTask.title }}</span>
                  </div>
                </label>

                <!-- Order Badge & Controls -->
                <div class="flex items-center gap-2">
                  <span *ngIf="isParentSelected(parentTask.id)" 
                        [class]="enableSequentialOrdering ? 'bg-indigo-600 text-white font-bold' : 'theme-subtle theme-text-heading font-medium border theme-border-subtle'"
                        class="text-[10px] px-2 py-0.5 rounded-full">
                    {{ enableSequentialOrdering ? 'Seq #' + getParentOrderIndex(parentTask.id) : 'Selected' }}
                  </span>

                  <div *ngIf="enableSequentialOrdering && isParentSelected(parentTask.id)" class="flex items-center gap-1">
                    <button type="button" (click)="moveOrderUp(parentTask.id)"
                            class="px-1.5 py-0.5 theme-btn-subtle text-[10px] rounded font-bold cursor-pointer">
                      &uarr;
                    </button>
                    <button type="button" (click)="moveOrderDown(parentTask.id)"
                            class="px-1.5 py-0.5 theme-btn-subtle text-[10px] rounded font-bold cursor-pointer">
                      &darr;
                    </button>
                  </div>
                </div>

              </div>
            </div>

            <!-- Ordered Sequence Display Summary -->
            <div *ngIf="enableSequentialOrdering && selectedParentTaskIds.length > 0" class="text-[11px] theme-text-muted bg-indigo-500/10 p-2 rounded-lg border border-indigo-500/20">
              <span class="font-bold text-indigo-400">Execution Sequence Order:</span>
              <span class="ml-1 theme-text-heading font-medium">
                {{ getFormattedSequenceSummary() }}
              </span>
            </div>
          </div>

          <!-- Submit Buttons -->
          <div class="flex items-center justify-end gap-2 pt-4 border-t theme-border-subtle">
            <button type="button" (click)="close.emit()"
                    class="px-4 py-2 theme-btn-subtle rounded-xl text-xs font-medium transition-all cursor-pointer">
              Cancel
            </button>
            <button type="submit" [disabled]="!title || loading || (isAssigneeOverloaded() && !overrideCircuitBreaker)"
                    class="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-xl text-xs font-semibold shadow-sm transition-all flex items-center gap-2 cursor-pointer">
              <span *ngIf="loading" class="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
              <span *ngIf="isAssigneeOverloaded() && !overrideCircuitBreaker">Blocked by Circuit Breaker</span>
              <span *ngIf="!isAssigneeOverloaded() || overrideCircuitBreaker">Create Task Card</span>
            </button>
          </div>

        </form>

      </div>
    </div>
  `
})
export class CreateTaskModalComponent implements OnInit {
  @Output() close = new EventEmitter<void>();

  title = '';
  description = '';
  environment: TaskEnvironment = 'DEVELOPMENT';
  deadlineInput = '';
  minDateTimeInput = '';
  assigneeId: number | null = null;
  overrideCircuitBreaker = false;
  
  customFactorApplied = false;

  selectedParentTaskIds: number[] = [];
  enableSequentialOrdering = true;

  loading = false;

  constructor(
    public taskService: TaskService,
    public settingsService: SettingsService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.taskService.loadUsers();
    this.settingsService.loadSettings();

    const now = new Date();
    const tzOffset = now.getTimezoneOffset() * 60000;
    const localNow = new Date(now.getTime() - tzOffset);
    this.minDateTimeInput = localNow.toISOString().slice(0, 16);

    const tomorrow = new Date(now);
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(17, 0, 0, 0);
    const localTomorrow = new Date(tomorrow.getTime() - tzOffset);
    this.deadlineInput = localTomorrow.toISOString().slice(0, 16);
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

  isParentSelected(taskId: number): boolean {
    return this.selectedParentTaskIds.includes(taskId);
  }

  getParentOrderIndex(taskId: number): number {
    return this.selectedParentTaskIds.indexOf(taskId) + 1;
  }

  toggleParentDependency(taskId: number): void {
    if (this.isParentSelected(taskId)) {
      this.selectedParentTaskIds = this.selectedParentTaskIds.filter(id => id !== taskId);
    } else {
      this.selectedParentTaskIds.push(taskId);
    }
  }

  moveOrderUp(taskId: number): void {
    const idx = this.selectedParentTaskIds.indexOf(taskId);
    if (idx > 0) {
      const temp = this.selectedParentTaskIds[idx - 1];
      this.selectedParentTaskIds[idx - 1] = taskId;
      this.selectedParentTaskIds[idx] = temp;
    }
  }

  moveOrderDown(taskId: number): void {
    const idx = this.selectedParentTaskIds.indexOf(taskId);
    if (idx >= 0 && idx < this.selectedParentTaskIds.length - 1) {
      const temp = this.selectedParentTaskIds[idx + 1];
      this.selectedParentTaskIds[idx + 1] = taskId;
      this.selectedParentTaskIds[idx] = temp;
    }
  }

  getFormattedSequenceSummary(): string {
    return this.selectedParentTaskIds
      .map((id, index) => `#${index + 1}: Task #${id}`)
      .join(' → ');
  }

  onSubmit(): void {
    if (!this.title || !this.deadlineInput) return;
    this.loading = true;

    const request: CreateTaskRequest = {
      title: this.title,
      description: this.description,
      environment: this.environment,
      customFactorApplied: this.customFactorApplied,
      deadline: new Date(this.deadlineInput).toISOString(),
      assigneeId: this.assigneeId ? Number(this.assigneeId) : undefined,
      parentTaskIds: this.selectedParentTaskIds,
      overrideCircuitBreaker: this.overrideCircuitBreaker
    };

    this.taskService.createTask(request).subscribe({
      next: () => {
        this.loading = false;
        this.close.emit();
      },
      error: () => this.loading = false
    });
  }
}

