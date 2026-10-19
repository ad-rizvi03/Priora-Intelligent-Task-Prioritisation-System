import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Task } from '../../models/task.model';
import { User } from '../../models/user.model';
import { TaskService } from '../../services/task.service';

/**
 * Feature 3: The 1-Click Ticket Defibrillator Modal.
 * Empowers developers and leads to revive or dissolve stagnating zombie blockers
 * through 4 decisive, high-impact algorithmic actions.
 */
@Component({
  selector: 'app-defibrillator-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed inset-0 bg-slate-950/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div class="bg-slate-900 border border-slate-800 rounded-3xl max-w-2xl w-full p-6 sm:p-8 shadow-2xl text-white space-y-6 relative overflow-hidden">
        
        <!-- Subtle Ambient Glow -->
        <div class="absolute -top-32 -right-32 w-64 h-64 bg-rose-500/10 rounded-full blur-3xl pointer-events-none"></div>
        <div class="absolute -bottom-32 -left-32 w-64 h-64 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none"></div>

        <!-- Header -->
        <div class="flex items-start justify-between border-b border-slate-800 pb-4 relative z-10">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-2xl bg-gradient-to-tr from-rose-500 to-amber-500 flex items-center justify-center font-black text-white text-lg shadow-lg shadow-rose-500/20">
              ⚡
            </div>
            <div>
              <div class="flex items-center gap-2">
                <h2 class="text-lg font-black text-white tracking-tight uppercase">Ticket Defibrillator</h2>
                <span [class]="getEntropyBadgeClasses()" class="text-[10px] font-extrabold uppercase px-2.5 py-0.5 rounded-full border">
                  {{ task.entropyLevel === 'ZOMBIE_BLOCKER' ? '🧟 ZOMBIE BLOCKER' : '⚠️ STAGNATING' }} ({{ task.entropyScore }}%)
                </span>
              </div>
              <p class="text-xs text-slate-400 font-medium mt-0.5">
                Task #{{ task.id }} &bull; {{ task.title }}
              </p>
            </div>
          </div>

          <button (click)="close.emit()" class="text-slate-400 hover:text-white text-2xl font-bold leading-none p-1 cursor-pointer">
            &times;
          </button>
        </div>

        <!-- Telemetry Diagnostics Bar -->
        <div class="bg-slate-950/80 border border-slate-800/80 rounded-2xl p-4 grid grid-cols-2 sm:grid-cols-4 gap-3 text-center relative z-10">
          <div class="p-2 bg-white/5 rounded-xl">
            <span class="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Inactivity</span>
            <span class="text-sm font-extrabold text-white">{{ task.hoursInactive }}h</span>
            <span class="text-[9px] text-slate-500 block">since update</span>
          </div>

          <div class="p-2 bg-white/5 rounded-xl">
            <span class="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Ignored Pings</span>
            <span class="text-sm font-extrabold text-amber-400">{{ task.pingCount }}</span>
            <span class="text-[9px] text-slate-500 block">alerts received</span>
          </div>

          <div class="p-2 bg-white/5 rounded-xl">
            <span class="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Downstream</span>
            <span class="text-sm font-extrabold text-rose-400">{{ task.directBlockedCount }} blocked</span>
            <span class="text-[9px] text-slate-500 block">waiting tasks</span>
          </div>

          <div class="p-2 bg-white/5 rounded-xl">
            <span class="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Trapped Drag</span>
            <span class="text-sm font-extrabold text-indigo-400">{{ task.totalGravityScore }} pts</span>
            <span class="text-[9px] text-slate-500 block">gravity mass</span>
          </div>
        </div>

        <!-- 4 Decisive Defibrillator Action Cards -->
        <div class="space-y-3 relative z-10">
          <span class="text-[10px] font-extrabold text-slate-400 uppercase tracking-widest block">
            Select 1-Click Intervention Strategy:
          </span>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
            
            <!-- Strategy 1: Split into Micro-Phases -->
            <div class="bg-gradient-to-b from-indigo-950/40 to-slate-950 border border-indigo-500/30 hover:border-indigo-500/60 rounded-2xl p-4 flex flex-col justify-between space-y-3 transition-all hover:shadow-lg hover:shadow-indigo-500/10">
              <div>
                <div class="flex items-center gap-2">
                  <span class="text-base">⚡</span>
                  <h3 class="text-xs font-black uppercase text-indigo-300 tracking-wide">Split into Micro-Phases</h3>
                </div>
                <p class="text-[11px] text-slate-400 mt-1.5 leading-relaxed">
                  Carve out immediate MVP deliverable as <strong>Phase 1</strong> and defer follow-up enhancements to <strong>Phase 2</strong> (+5 days). Clears downstream roadblocks today.
                </p>
              </div>
              <button (click)="onExecute('SPLIT')" [disabled]="executing"
                      class="w-full py-2 bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs rounded-xl shadow-md transition-all cursor-pointer">
                Split & Unblock MVP
              </button>
            </div>

            <!-- Strategy 2: Shed Dead Weight -->
            <div class="bg-gradient-to-b from-amber-950/40 to-slate-950 border border-amber-500/30 hover:border-amber-500/60 rounded-2xl p-4 flex flex-col justify-between space-y-3 transition-all hover:shadow-lg hover:shadow-amber-500/10">
              <div>
                <div class="flex items-center gap-2">
                  <span class="text-base">🪶</span>
                  <h3 class="text-xs font-black uppercase text-amber-300 tracking-wide">Shed Dead Weight</h3>
                </div>
                <p class="text-[11px] text-slate-400 mt-1.5 leading-relaxed">
                  Relieve sprint pressure. Extends deadline by <strong>+7 days</strong>, clears ignored pings to zero, and strips custom multipliers to restore team breathing room.
                </p>
              </div>
              <button (click)="onExecute('SHED_GRAVITY')" [disabled]="executing"
                      class="w-full py-2 bg-amber-600 hover:bg-amber-500 text-white font-bold text-xs rounded-xl shadow-md transition-all cursor-pointer">
                Shed Gravity (+7 Days)
              </button>
            </div>

            <!-- Strategy 3: Smart Re-Route -->
            <div class="bg-gradient-to-b from-emerald-950/40 to-slate-950 border border-emerald-500/30 hover:border-emerald-500/60 rounded-2xl p-4 flex flex-col justify-between space-y-3 transition-all hover:shadow-lg hover:shadow-emerald-500/10">
              <div>
                <div class="flex items-center gap-2">
                  <span class="text-base">🎯</span>
                  <h3 class="text-xs font-black uppercase text-emerald-300 tracking-wide">Smart Re-Route</h3>
                </div>
                <p class="text-[11px] text-slate-400 mt-1.5 leading-relaxed">
                  Pass the torch to the teammate with the lowest cognitive load:
                  <strong class="text-emerald-400">{{ getLeastLoadedUser()?.fullName || 'Available Engineer' }}</strong>
                  ({{ getLeastLoadedUser()?.capacityPercentage || 0 }}% load).
                </p>
              </div>
              <button (click)="onExecute('SMART_PASS')" [disabled]="executing"
                      class="w-full py-2 bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs rounded-xl shadow-md transition-all cursor-pointer">
                Re-Route to {{ getLeastLoadedUser()?.fullName?.split(' ')?.at(0) || 'Teammate' }}
              </button>
            </div>

            <!-- Strategy 4: Dissolve Blocker -->
            <div class="bg-gradient-to-b from-slate-800/40 to-slate-950 border border-slate-700 hover:border-slate-500 rounded-2xl p-4 flex flex-col justify-between space-y-3 transition-all">
              <div>
                <div class="flex items-center gap-2">
                  <span class="text-base">🕊️</span>
                  <h3 class="text-xs font-black uppercase text-slate-300 tracking-wide">Dissolve Blocker</h3>
                </div>
                <p class="text-[11px] text-slate-400 mt-1.5 leading-relaxed">
                  Declare victory or archive. Marks task as <strong>DONE</strong> with zero penalty, immediately dissolving all trapped drag across the dependency graph.
                </p>
              </div>
              <button (click)="onExecute('DISSOLVE')" [disabled]="executing"
                      class="w-full py-2 bg-slate-700 hover:bg-slate-600 text-white font-bold text-xs rounded-xl transition-all cursor-pointer">
                Dissolve & Release
              </button>
            </div>

          </div>
        </div>

      </div>
    </div>
  `
})
export class DefibrillatorModalComponent {
  @Input({ required: true }) task!: Task;
  @Output() close = new EventEmitter<void>();

  public executing = false;

  constructor(public taskService: TaskService) {}

  getEntropyBadgeClasses(): string {
    if (this.task.entropyLevel === 'ZOMBIE_BLOCKER') {
      return 'bg-rose-500/20 text-rose-300 border-rose-500/40 animate-pulse';
    }
    return 'bg-amber-500/20 text-amber-300 border-amber-500/40';
  }

  getLeastLoadedUser(): User | undefined {
    const assignable = this.taskService.users().filter(u => u.id !== this.task.assignee?.id);
    if (!assignable.length) return this.taskService.users().at(0);
    return assignable.slice().sort((a, b) => (a.activeGravityLoad || 0) - (b.activeGravityLoad || 0)).at(0);
  }

  onExecute(action: 'SPLIT' | 'SHED_GRAVITY' | 'SMART_PASS' | 'DISSOLVE'): void {
    this.executing = true;
    const targetUserId = action === 'SMART_PASS' ? this.getLeastLoadedUser()?.id : undefined;
    
    this.taskService.defibrillateTask(this.task.id, action, targetUserId).subscribe({
      next: () => {
        this.executing = false;
        this.close.emit();
      },
      error: () => {
        this.executing = false;
      }
    });
  }
}
