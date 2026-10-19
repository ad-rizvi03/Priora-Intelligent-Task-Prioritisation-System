import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaskService } from '../../services/task.service';
import { TaskCardComponent } from '../task-card/task-card.component';
import { Task } from '../../models/task.model';
import { User } from '../../models/user.model';
import { AuthService } from '../../services/auth.service';
import { DefibrillatorModalComponent } from '../defibrillator-modal/defibrillator-modal.component';
import { SprintSimulatorModalComponent } from '../sprint-simulator-modal/sprint-simulator-modal.component';

/**
 * 3-Zone Visual Priority Dashboard Canvas.
 * Standardized uniform grid layout (`grid-cols-1 md:grid-cols-2 lg:grid-cols-3`) across all 3 zones.
 * Zero emojis - clean executive presentation.
 */
@Component({
  selector: 'app-canvas',
  standalone: true,
  imports: [CommonModule, TaskCardComponent, DefibrillatorModalComponent, SprintSimulatorModalComponent],
  template: `
    <div class="max-w-7xl mx-auto px-6 py-8 space-y-10">
      
      <!-- Error Message Banner -->
      <div *ngIf="taskService.errorMessage()" 
           class="bg-rose-50 border border-rose-200 text-rose-800 p-4 rounded-xl text-xs font-semibold flex items-center justify-between shadow-xs">
        <span>{{ taskService.errorMessage() }}</span>
        <button (click)="taskService.loadTasks()" class="px-3 py-1 bg-rose-600 text-white rounded-lg hover:bg-rose-700 text-xs transition-all">
          Retry Connection
        </button>
      </div>

      <!-- Loading Spinner -->
      <div *ngIf="taskService.loading()" class="flex flex-col items-center justify-center py-16 space-y-3">
        <div class="w-8 h-8 border-3 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
        <p class="text-xs font-semibold text-slate-500">Calculating Dynamic Priora Scores...</p>
      </div>

      <ng-container *ngIf="!taskService.loading()">

        <!-- Executive Action Bar: What-If Simulator Trigger -->
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-1 border-b theme-border-subtle">
          <div>
            <h1 class="text-xl font-black theme-text-heading tracking-tight">Gravitational Priority Canvas</h1>
            <p class="text-xs theme-text-muted font-medium">Continuous Workload Defense &bull; Dynamic Orbit Mechanics</p>
          </div>
          <button (click)="taskService.openSimulationModal()"
                  class="px-4 py-2 bg-gradient-to-r from-cyan-600 via-indigo-600 to-purple-600 hover:from-cyan-500 hover:to-purple-500 text-white font-extrabold text-xs rounded-xl shadow-md transition-all flex items-center gap-2 cursor-pointer self-start sm:self-auto">
            <span class="text-sm">🔮</span>
            <span>What-If Sprint Time Machine</span>
            <span class="bg-white/20 text-white text-[10px] px-1.5 py-0.5 rounded-full font-mono">Sandbox</span>
          </button>
        </div>

        <!-- Team Workload Defense Status Banner -->
        <div class="bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 text-white rounded-2xl p-5 shadow-md flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border border-indigo-900/50">
          <div class="space-y-1">
            <div class="flex items-center gap-2">
              <span class="text-xs font-bold uppercase tracking-wider text-indigo-400 flex items-center gap-1.5">
                <span>🛡️</span> Gravitational Workload Defense
              </span>
              <span *ngIf="getSupernovaUsers().length > 0" class="bg-rose-500/20 text-rose-300 border border-rose-500/40 text-[10px] font-extrabold px-2.5 py-0.5 rounded-full flex items-center gap-1.5 animate-pulse">
                <span class="w-1.5 h-1.5 rounded-full bg-rose-400"></span>
                Circuit Breaker Engaged ({{ getSupernovaUsers().length }} Supernova Lock)
              </span>
              <span *ngIf="getSupernovaUsers().length === 0" class="bg-emerald-500/20 text-emerald-300 border border-emerald-500/40 text-[10px] font-bold px-2.5 py-0.5 rounded-full flex items-center gap-1.5">
                <span class="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
                Team Capacity Nominal
              </span>
            </div>
            <p class="text-xs text-slate-300">
              <span *ngIf="getSupernovaUsers().length > 0">
                <strong>{{ getSupernovaUsers()[0].fullName }}</strong> has exceeded critical gravitational mass. Task assignments are dynamically guarded to prevent sprint failure.
              </span>
              <span *ngIf="getSupernovaUsers().length === 0">
                All engineers are operating within sustainable gravitational limits.
              </span>
            </p>
          </div>

          <div class="flex items-center gap-2.5 text-xs">
            <div class="flex items-center gap-1.5 bg-white/10 px-3 py-1.5 rounded-xl backdrop-blur-xs">
              <span class="w-2 h-2 rounded-full bg-emerald-400"></span>
              <span class="font-semibold text-slate-200">{{ getHealthyUsers().length }} Nominal</span>
            </div>
            <div *ngIf="getHeavyUsers().length > 0" class="flex items-center gap-1.5 bg-amber-500/20 border border-amber-500/30 px-3 py-1.5 rounded-xl">
              <span class="w-2 h-2 rounded-full bg-amber-400"></span>
              <span class="font-semibold text-amber-200">{{ getHeavyUsers().length }} Heavy</span>
            </div>
            <div *ngIf="getSupernovaUsers().length > 0" class="flex items-center gap-1.5 bg-rose-500/20 border border-rose-500/40 px-3 py-1.5 rounded-xl">
              <span class="w-2 h-2 rounded-full bg-rose-400 animate-ping"></span>
              <span class="font-extrabold text-rose-200">{{ getSupernovaUsers().length }} Supernova</span>
            </div>
          </div>
        </div>

        <!-- Feature 3: Sprint Entropy Decay Alert Bar -->
        <div *ngIf="getZombieBlockerTasks().length > 0 || getStagnatingTasks().length > 0" 
             class="bg-gradient-to-r from-purple-950 via-slate-900 to-purple-950 border border-purple-500/40 rounded-2xl p-4 text-white shadow-md flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-xl bg-purple-500/20 text-purple-300 border border-purple-500/40 flex items-center justify-center font-bold text-sm shrink-0">
              🧟
            </div>
            <div>
              <div class="flex items-center gap-2">
                <span class="text-xs font-bold uppercase tracking-wider text-purple-300">Entropy Decay Radar</span>
                <span *ngIf="getZombieBlockerTasks().length > 0" class="bg-rose-500/20 text-rose-300 border border-rose-500/40 text-[10px] font-extrabold px-2 py-0.5 rounded-full animate-pulse">
                  {{ getZombieBlockerTasks().length }} Zombie Blocker(s)
                </span>
                <span *ngIf="getStagnatingTasks().length > 0" class="bg-amber-500/20 text-amber-300 border border-amber-500/40 text-[10px] font-bold px-2 py-0.5 rounded-full">
                  {{ getStagnatingTasks().length }} Stagnating
                </span>
              </div>
              <p class="text-xs text-slate-300 mt-0.5">
                Stalled tickets detected with zero forward velocity. Use the <strong>"⚡ Defib"</strong> button on any flagged card to revive, shed dead weight, or dissolve.
              </p>
            </div>
          </div>

          <div class="flex items-center gap-2 shrink-0">
            <button *ngIf="getZombieBlockerTasks().length > 0"
                    (click)="taskService.openDefibrillatorModal(getZombieBlockerTasks()[0])"
                    class="px-3 py-1.5 bg-gradient-to-r from-rose-500 to-purple-600 hover:from-rose-600 hover:to-purple-700 text-white font-extrabold text-xs rounded-xl shadow-md transition-all flex items-center gap-1.5 cursor-pointer">
              <span>⚡</span> Defibrillate Top Blocker
            </button>
          </div>
        </div>

        <!-- THE DOMINO OF THE DAY: Algorithmic Reverse Blast Radius Showcase -->
        <div *ngIf="getDominoTask() as domino" 
             class="relative overflow-hidden bg-gradient-to-br from-slate-950 via-slate-900 to-amber-950/70 border-2 border-amber-500/50 rounded-3xl p-6 shadow-xl text-white">
          
          <!-- Background Ambient Glow -->
          <div class="absolute -top-24 -right-24 w-72 h-72 bg-amber-500/10 rounded-full blur-3xl pointer-events-none"></div>
          <div class="absolute -bottom-24 -left-24 w-72 h-72 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none"></div>

          <div class="relative z-10 space-y-5">
            <!-- Header Row -->
            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-white/10 pb-4">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-2xl bg-gradient-to-tr from-amber-500 to-yellow-400 text-slate-950 flex items-center justify-center font-black shadow-lg shadow-amber-500/20 text-lg">
                  🎯
                </div>
                <div>
                  <div class="flex items-center gap-2">
                    <h3 class="text-base font-extrabold tracking-tight text-white uppercase">The Domino of the Day</h3>
                    <span class="bg-amber-400 text-slate-950 text-[10px] font-black uppercase tracking-wider px-2 py-0.5 rounded-full shadow-2xs">
                      Highest Leverage
                    </span>
                  </div>
                  <p class="text-xs text-slate-400 font-medium">
                    Algorithmic Reverse Blast Radius &bull; The single ticket whose completion produces the highest positive chain reaction
                  </p>
                </div>
              </div>

              <!-- Leverage Score Counter Pill -->
              <div class="flex items-center gap-2 self-start sm:self-center bg-white/5 border border-white/10 px-3.5 py-1.5 rounded-2xl backdrop-blur-md">
                <span class="text-[10px] text-amber-400 font-bold uppercase tracking-wider">Leverage Index</span>
                <span class="text-lg font-black text-amber-300 leading-none">{{ domino.dominoLeverageScore || domino.totalGravityScore }}</span>
              </div>
            </div>

            <!-- Main Body Grid: Task Spotlight + Downstream Impact Simulation -->
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-5 items-stretch">
              
              <!-- Left: Target Task Details (5 cols) -->
              <div class="lg:col-span-5 bg-white/5 border border-white/10 rounded-2xl p-4 flex flex-col justify-between space-y-3">
                <div class="space-y-1.5">
                  <div class="flex items-center gap-2">
                    <span [class]="getEnvBadgeClasses(domino.environment)" class="text-[10px] font-bold tracking-wider uppercase px-2 py-0.5 rounded border">
                      {{ domino.environment }}
                    </span>
                    <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">
                      {{ domino.canvasZone }} ZONE
                    </span>
                    <span class="text-xs text-slate-400">&bull;</span>
                    <span class="text-xs text-slate-300 font-mono">#{{ domino.id }}</span>
                  </div>
                  <h4 class="text-base font-bold text-white tracking-tight leading-snug">
                    {{ domino.title }}
                  </h4>
                  <p class="text-xs text-slate-400 line-clamp-2">
                    {{ domino.description || 'No description provided.' }}
                  </p>
                </div>

                <div class="pt-2 border-t border-white/10 flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <div class="w-6 h-6 rounded-full bg-amber-400/20 text-amber-300 border border-amber-400/30 flex items-center justify-center font-bold text-[10px]">
                      {{ domino.assignee?.fullName?.charAt(0) || 'U' }}
                    </div>
                    <span class="text-xs font-medium text-slate-300">
                      {{ domino.assignee?.fullName || 'Unassigned' }}
                    </span>
                  </div>

                  <div class="text-right">
                    <span class="text-[10px] text-slate-400 uppercase block">Gravity</span>
                    <span class="text-xs font-bold text-white">{{ domino.totalGravityScore }} pts</span>
                  </div>
                </div>
              </div>

              <!-- Right: Downstream Cascade Simulation (7 cols) -->
              <div class="lg:col-span-7 bg-amber-500/10 border border-amber-500/20 rounded-2xl p-4 flex flex-col justify-between space-y-3">
                <div>
                  <span class="text-[10px] font-extrabold uppercase tracking-widest text-amber-400 block mb-2">
                    Predictive Downstream Impact Simulation
                  </span>
                  
                  <div class="grid grid-cols-3 gap-3">
                    <div class="bg-black/30 border border-white/10 rounded-xl p-3 text-center">
                      <span class="text-2xl font-black text-amber-300 block leading-tight">
                        {{ domino.unblockedTasksCount || domino.directBlockedCount }}
                      </span>
                      <span class="text-[10px] text-slate-300 font-semibold uppercase tracking-tight block">
                        Tasks Unblocked
                      </span>
                      <span class="text-[9px] text-slate-400 block mt-0.5">
                        Direct & downstream
                      </span>
                    </div>

                    <div class="bg-black/30 border border-white/10 rounded-xl p-3 text-center">
                      <span class="text-2xl font-black text-cyan-300 block leading-tight">
                        {{ domino.unblockedEngineersCount || (domino.directBlockedCount > 0 ? 1 : 0) }}
                      </span>
                      <span class="text-[10px] text-slate-300 font-semibold uppercase tracking-tight block">
                        Engineers Unstuck
                      </span>
                      <span class="text-[9px] text-slate-400 block mt-0.5">
                        Idle/trapped capacity
                      </span>
                    </div>

                    <div class="bg-black/30 border border-white/10 rounded-xl p-3 text-center">
                      <span class="text-2xl font-black text-emerald-300 block leading-tight">
                        -{{ domino.releasedGravityPoints || domino.dependencyScore }}
                      </span>
                      <span class="text-[10px] text-slate-300 font-semibold uppercase tracking-tight block">
                        Gravity Released
                      </span>
                      <span class="text-[9px] text-slate-400 block mt-0.5">
                        Relieves team drag
                      </span>
                    </div>
                  </div>
                </div>

                <!-- Simulation Insight Bar -->
                <div class="bg-black/40 rounded-xl p-2.5 border border-white/5 flex items-center justify-between text-xs gap-3">
                  <p class="text-slate-300 text-[11px] leading-relaxed">
                    <strong class="text-amber-300">Cascade Forecast:</strong> Resolving this single ticket eliminates <span class="font-bold text-white">{{ domino.releasedGravityPoints || domino.dependencyScore }} pts</span> of trapped drag and clears roadblocked work across the team.
                  </p>
                  <button (click)="markDominoDone(domino)" 
                          class="shrink-0 px-3 py-1.5 bg-gradient-to-r from-amber-500 to-yellow-400 hover:from-amber-400 hover:to-yellow-300 text-slate-950 font-bold text-xs rounded-xl shadow-md transition-all flex items-center gap-1.5 cursor-pointer">
                    <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                    </svg>
                    Mark Done & Release
                  </button>
                </div>

              </div>

            </div>
          </div>
        </div>

        <!-- ZONE 1: FOCUS ZONE (Score >= 80.0) -->
        <section class="space-y-4">
          <div class="flex items-center justify-between border-b theme-border-subtle pb-3">
            <div class="flex items-center gap-3">
              <div class="w-3 h-3 rounded-full bg-indigo-600"></div>
              <h2 class="text-lg font-bold theme-text-heading tracking-tight">Focus Zone</h2>
              <span class="text-xs font-semibold text-indigo-400 bg-indigo-500/10 border border-indigo-500/20 px-2.5 py-0.5 rounded-full">
                Score &ge; 80.0
              </span>
            </div>
            <span class="text-xs font-semibold theme-text-muted">
              {{ taskService.focusZoneTasks().length }} Critical Tasks
            </span>
          </div>

          <!-- Empty Focus State -->
          <div *ngIf="taskService.focusZoneTasks().length === 0" 
               class="theme-card rounded-2xl p-8 text-center border border-dashed theme-border-subtle theme-text-muted text-xs font-medium">
            No critical tasks in the Focus Zone.
          </div>

          <!-- Uniform Grid Layout -->
          <div *ngIf="taskService.focusZoneTasks().length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <app-task-card *ngFor="let task of taskService.focusZoneTasks()" [task]="task">
            </app-task-card>
          </div>
        </section>

        <!-- ZONE 2: DRIFTING ZONE (Score 40.0 - 80.0) -->
        <section class="space-y-4">
          <div class="flex items-center justify-between border-b theme-border-subtle pb-3">
            <div class="flex items-center gap-3">
              <div class="w-3 h-3 rounded-full bg-amber-500"></div>
              <h2 class="text-lg font-bold theme-text-heading tracking-tight">Drifting Zone</h2>
              <span class="text-xs font-semibold text-amber-400 bg-amber-500/10 border border-amber-500/20 px-2.5 py-0.5 rounded-full">
                Score 40.0 - 80.0
              </span>
            </div>
            <span class="text-xs font-semibold theme-text-muted">
              {{ taskService.driftingZoneTasks().length }} Active Tasks
            </span>
          </div>

          <!-- Empty Drifting State -->
          <div *ngIf="taskService.driftingZoneTasks().length === 0" 
               class="theme-card rounded-2xl p-8 text-center border border-dashed theme-border-subtle theme-text-muted text-xs font-medium">
            No active tasks in the Drifting Zone.
          </div>

          <!-- Uniform Grid Layout -->
          <div *ngIf="taskService.driftingZoneTasks().length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <app-task-card *ngFor="let task of taskService.driftingZoneTasks()" [task]="task">
            </app-task-card>
          </div>
        </section>

        <!-- ZONE 3: ORBIT ZONE (Score < 40.0 / Completed Tasks) -->
        <section class="space-y-4">
          <div class="flex items-center justify-between border-b theme-border-subtle pb-3">
            <div class="flex items-center gap-3">
              <div class="w-3 h-3 rounded-full bg-slate-400"></div>
              <h2 class="text-lg font-bold theme-text-heading tracking-tight">Orbit Zone</h2>
              <span class="text-xs font-semibold theme-text-muted theme-subtle border theme-border-subtle px-2.5 py-0.5 rounded-full">
                Score &lt; 40.0 / Done
              </span>
            </div>
            <span class="text-xs font-semibold theme-text-muted">
              {{ taskService.orbitZoneTasks().length }} Backlog / Completed Tasks
            </span>
          </div>

          <!-- Empty Orbit State -->
          <div *ngIf="taskService.orbitZoneTasks().length === 0" 
               class="theme-card rounded-2xl p-8 text-center border border-dashed theme-border-subtle theme-text-muted text-xs font-medium">
            No backlog or completed tasks in the Orbit Zone.
          </div>

          <!-- Uniform Grid Layout (Matches Focus Zone: 3 Columns) -->
          <div *ngIf="taskService.orbitZoneTasks().length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <app-task-card *ngFor="let task of taskService.orbitZoneTasks()" [task]="task">
            </app-task-card>
          </div>
        </section>

      </ng-container>

      <!-- Feature 3: Defibrillator Modal -->
      <app-defibrillator-modal *ngIf="taskService.selectedTaskForDefibrillator() as defibTask"
                               [task]="defibTask"
                               (close)="taskService.closeDefibrillatorModal()">
      </app-defibrillator-modal>

      <!-- Feature 4: What-If Sprint Simulator Modal -->
      <app-sprint-simulator-modal *ngIf="taskService.simulationModalOpen()"
                                  (close)="taskService.closeSimulationModal()">
      </app-sprint-simulator-modal>

    </div>
  `
})
export class CanvasComponent implements OnInit {
  constructor(
    public taskService: TaskService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.taskService.loadTasks();
    this.taskService.loadUsers();
  }

  getDominoTask(): Task | undefined {
    return this.taskService.tasks().find(t => t.dominoOfDay && t.status !== 'DONE');
  }

  markDominoDone(task: Task): void {
    this.taskService.updateStatus(task.id, 'DONE').subscribe();
  }

  getEnvBadgeClasses(env: string): string {
    switch (env) {
      case 'PRODUCTION': return 'bg-rose-500/20 text-rose-300 border-rose-500/30';
      case 'STAGING': return 'bg-amber-500/20 text-amber-300 border-amber-500/30';
      default: return 'bg-slate-500/20 text-slate-300 border-slate-500/30';
    }
  }

  getZombieBlockerTasks(): Task[] {
    return this.taskService.tasks().filter(t => t.entropyLevel === 'ZOMBIE_BLOCKER' && t.status !== 'DONE');
  }

  getStagnatingTasks(): Task[] {
    return this.taskService.tasks().filter(t => t.entropyLevel === 'STAGNATING' && t.status !== 'DONE');
  }

  getSupernovaUsers(): User[] {
    return this.taskService.users().filter(u => u.burnoutStatus === 'SUPERNOVA_LOCKED');
  }

  getHeavyUsers(): User[] {
    return this.taskService.users().filter(u => u.burnoutStatus === 'HEAVY');
  }

  getHealthyUsers(): User[] {
    return this.taskService.users().filter(u => u.burnoutStatus === 'NORMAL' || !u.burnoutStatus);
  }
}

