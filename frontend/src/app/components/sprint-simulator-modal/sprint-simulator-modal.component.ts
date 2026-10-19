import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Task } from '../../models/task.model';
import { User } from '../../models/user.model';
import { TaskService, SprintSimulationRequest, SprintSimulationResponse } from '../../services/task.service';

/**
 * Feature 4: What-If Sprint Time Machine & Cascade Simulator.
 * Allows engineering leads and PMs to simulate bottleneck slips forward in time,
 * inspect the chronological failure wave, and execute 1-click algorithmic mitigations.
 */
@Component({
  selector: 'app-sprint-simulator-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="fixed inset-0 bg-slate-950/75 backdrop-blur-md z-50 flex items-center justify-center p-4">
      <div class="bg-slate-900 border border-slate-800 rounded-3xl max-w-3xl w-full p-6 sm:p-8 shadow-2xl text-white space-y-6 relative overflow-hidden max-h-[92vh] flex flex-col">
        
        <!-- Background Ambient Glow -->
        <div class="absolute -top-32 -right-32 w-72 h-72 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none"></div>
        <div class="absolute -bottom-32 -left-32 w-72 h-72 bg-purple-500/10 rounded-full blur-3xl pointer-events-none"></div>

        <!-- Header -->
        <div class="flex items-start justify-between border-b border-slate-800 pb-4 shrink-0 relative z-10">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-2xl bg-gradient-to-tr from-cyan-500 to-indigo-600 flex items-center justify-center font-black text-white text-xl shadow-lg shadow-indigo-500/20">
              🔮
            </div>
            <div>
              <div class="flex items-center gap-2">
                <h2 class="text-lg font-black text-white tracking-tight uppercase">What-If Sprint Time Machine</h2>
                <span class="bg-indigo-500/20 text-indigo-300 border border-indigo-500/40 text-[10px] font-extrabold uppercase px-2.5 py-0.5 rounded-full">
                  Cascade Sandbox
                </span>
              </div>
              <p class="text-xs text-slate-400 font-medium mt-0.5">
                Predictive Blast Radius &bull; Simulate schedule slips forward in time before they wreck sprint delivery
              </p>
            </div>
          </div>

          <button (click)="close.emit()" class="text-slate-400 hover:text-white text-2xl font-bold leading-none p-1 cursor-pointer">
            &times;
          </button>
        </div>

        <div class="overflow-y-auto space-y-6 pr-1 relative z-10">
          
          <!-- Control Panel: Target Bottleneck & Time Sliders -->
          <div class="bg-slate-950/80 border border-slate-800/80 rounded-2xl p-5 space-y-4">
            <span class="text-[10px] font-extrabold text-indigo-400 uppercase tracking-widest block">
              1. Configure Stress Test Scenarios
            </span>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <!-- Select Target Bottleneck Task -->
              <div class="space-y-1.5">
                <label class="block text-xs font-semibold text-slate-300">Simulate Delay On Task</label>
                <select [(ngModel)]="selectedTaskId" (ngModelChange)="runSimulation()"
                        class="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-indigo-500">
                  <option *ngFor="let t of getActiveTasks()" [value]="t.id">
                    #{{ t.id }} - {{ t.title }} ({{ t.canvasZone }} • {{ t.totalGravityScore }} pts)
                  </option>
                </select>
              </div>

              <!-- Select Capacity Outage (Optional) -->
              <div class="space-y-1.5">
                <label class="block text-xs font-semibold text-slate-300">Simulate Capacity Loss (Out Sick / Incident)</label>
                <select [(ngModel)]="unavailableUserId" (ngModelChange)="runSimulation()"
                        class="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-indigo-500">
                  <option [ngValue]="null">-- All Engineers Available (Nominal) --</option>
                  <option *ngFor="let u of taskService.users()" [value]="u.id">
                    {{ u.fullName }} ({{ u.role === 'ROLE_TESTER' ? 'Tester' : 'Developer' }} • {{ u.activeGravityLoad }} pts)
                  </option>
                </select>
              </div>
            </div>

            <!-- Delay Time Slider -->
            <div class="space-y-2 pt-2 border-t border-slate-800/80">
              <div class="flex items-center justify-between">
                <label class="text-xs font-semibold text-slate-300">Projected Slippage Duration</label>
                <span class="text-xs font-black text-cyan-400 bg-cyan-950/80 border border-cyan-800 px-2.5 py-0.5 rounded-lg">
                  +{{ delayHours }} Hours ({{ (delayHours / 24).toFixed(1) }} Days)
                </span>
              </div>
              <input type="range" [(ngModel)]="delayHours" (ngModelChange)="runSimulation()" min="12" max="96" step="12"
                     class="w-full accent-cyan-400 cursor-pointer h-2 bg-slate-800 rounded-lg">
              <div class="flex justify-between text-[10px] text-slate-500 font-mono">
                <span>+12h (Half Day)</span>
                <span>+24h (1 Day)</span>
                <span>+48h (2 Days)</span>
                <span>+72h (3 Days)</span>
                <span>+96h (4 Days)</span>
              </div>
            </div>
          </div>

          <!-- Live Predictive Telemetry & Risk Meter -->
          <div *ngIf="simulationResult as sim" class="space-y-4">
            
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-3.5">
              
              <!-- Projected Risk Meter Card -->
              <div class="p-4 rounded-2xl border" [class]="getRiskCardClasses(sim.riskLevel)">
                <span class="text-[10px] font-extrabold uppercase tracking-widest block opacity-80">Sprint Delivery Risk</span>
                <span class="text-3xl font-black mt-1 block leading-tight">{{ sim.projectedFailureRisk }}%</span>
                <span class="text-[10px] font-bold uppercase tracking-wider block mt-1">
                  {{ sim.riskLevel.replace('_', ' ') }}
                </span>
              </div>

              <!-- Trapped Gravity Drag Shock -->
              <div class="p-4 rounded-2xl bg-slate-950/80 border border-slate-800">
                <span class="text-[10px] font-bold text-slate-400 uppercase tracking-widest block">Systemic Drag Shock</span>
                <span class="text-3xl font-black text-indigo-400 mt-1 block leading-tight">+{{ sim.dragIncreasePoints }}</span>
                <span class="text-[10px] text-slate-400 block mt-1">added gravity points</span>
              </div>

              <!-- Breached Downstream Deliverables -->
              <div class="p-4 rounded-2xl bg-slate-950/80 border border-slate-800">
                <span class="text-[10px] font-bold text-slate-400 uppercase tracking-widest block">Downstream Breaches</span>
                <span class="text-3xl font-black mt-1 block leading-tight" [class]="sim.overdueTasksCount > 0 ? 'text-rose-400' : 'text-emerald-400'">
                  {{ sim.overdueTasksCount }} / {{ sim.impactedTasksCount }}
                </span>
                <span class="text-[10px] text-slate-400 block mt-1">deadlines blown</span>
              </div>

            </div>

            <!-- Cascade Failure Wave Timeline -->
            <div class="bg-slate-950/80 border border-slate-800/80 rounded-2xl p-4 space-y-3">
              <span class="text-[10px] font-extrabold text-slate-400 uppercase tracking-widest block">
                Chronological Domino Failure Wave:
              </span>
              <div class="space-y-2">
                <div *ngFor="let step of sim.cascadeSteps" 
                     class="text-xs p-2.5 rounded-xl border border-white/5 bg-white/5 text-slate-300 leading-relaxed font-mono">
                  {{ step }}
                </div>
              </div>
            </div>

            <!-- 1-Click Algorithmic Mitigation Plan -->
            <div class="bg-gradient-to-r from-emerald-950/60 via-slate-950 to-emerald-950/60 border border-emerald-500/40 rounded-2xl p-5 space-y-3">
              <div class="flex items-center gap-2">
                <span class="text-lg">🛡️</span>
                <h3 class="text-xs font-black uppercase text-emerald-300 tracking-wider">Algorithmic Mitigation Plan</h3>
              </div>
              <p class="text-xs text-slate-200 leading-relaxed">
                {{ sim.recommendedMitigationPlan }}
              </p>
              
              <div class="pt-2 flex items-center justify-between gap-3">
                <span class="text-[11px] text-emerald-400 font-semibold">
                  1-Click re-routes tasks and buffers deadlines to restore 100% green sprint orbit.
                </span>
                <button (click)="onApplyMitigation()" [disabled]="applying"
                        class="px-4 py-2 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-400 hover:to-teal-400 text-slate-950 font-black text-xs rounded-xl shadow-lg transition-all shrink-0 cursor-pointer">
                  {{ applying ? 'Applying Plan...' : '⚡ Apply Algorithmic Mitigation' }}
                </button>
              </div>
            </div>

          </div>

        </div>

      </div>
    </div>
  `
})
export class SprintSimulatorModalComponent implements OnInit {
  @Output() close = new EventEmitter<void>();

  public selectedTaskId: number | null = null;
  public delayHours = 24;
  public unavailableUserId: number | null = null;
  public simulationResult: SprintSimulationResponse | null = null;
  public applying = false;

  constructor(public taskService: TaskService) {}

  ngOnInit(): void {
    const topTask = this.getActiveTasks().at(0);
    if (topTask) {
      this.selectedTaskId = topTask.id;
    }
    this.runSimulation();
  }

  getActiveTasks(): Task[] {
    return this.taskService.tasks().filter(t => t.status !== 'DONE');
  }

  runSimulation(): void {
    const req: SprintSimulationRequest = {
      delayedTaskId: this.selectedTaskId ?? undefined,
      delayHours: this.delayHours,
      unavailableUserId: this.unavailableUserId ?? undefined
    };

    this.taskService.simulateSprint(req).subscribe({
      next: (res) => {
        this.simulationResult = res;
      }
    });
  }

  getRiskCardClasses(level: string): string {
    switch (level) {
      case 'CRITICAL_COLLAPSE':
        return 'bg-rose-950/80 border-rose-500 text-rose-300 animate-pulse';
      case 'ELEVATED':
        return 'bg-amber-950/80 border-amber-500 text-amber-300';
      default:
        return 'bg-emerald-950/80 border-emerald-500 text-emerald-300';
    }
  }

  onApplyMitigation(): void {
    this.applying = true;
    const req: SprintSimulationRequest = {
      delayedTaskId: this.selectedTaskId ?? undefined,
      delayHours: this.delayHours,
      unavailableUserId: this.unavailableUserId ?? undefined
    };

    this.taskService.applyMitigation(req).subscribe({
      next: () => {
        this.applying = false;
        this.close.emit();
      },
      error: () => {
        this.applying = false;
      }
    });
  }
}
