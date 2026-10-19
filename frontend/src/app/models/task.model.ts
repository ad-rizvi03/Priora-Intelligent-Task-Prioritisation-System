import { User } from './user.model';

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE' | 'BLOCKED';
export type TaskEnvironment = 'PRODUCTION' | 'STAGING' | 'DEVELOPMENT';
export type CanvasZone = 'FOCUS' | 'DRIFTING' | 'ORBIT';
export type ApprovalStatus = 'APPROVED' | 'PENDING_APPROVAL' | 'REJECTED';

export interface Task {
  id: number;
  title: string;
  description: string;
  status: TaskStatus;
  approvalStatus: ApprovalStatus;
  rejectionReason?: string;
  environment: TaskEnvironment;
  customFactorApplied?: boolean;
  pinged?: boolean;
  assignee: User | null;
  createdBy: User | null;
  createdAt: string;
  lastActivityAt: string;
  deadline: string;
  pingCount: number;
  dependentTaskIds: number[];
  dependencyIds: number[];

  // Dynamic Gravity Score Breakdown
  totalGravityScore: number;
  dependencyScore: number;
  directBlockedCount: number;
  indirectBlockedCount: number;
  deadlineProximityScore: number;
  hoursUntilDeadline: number;
  overdue: boolean;
  stagnationScore: number;
  hoursInactive: number;
  workloadDensityScore: number;
  assigneeActiveTasksCount: number;
  customFactorName?: string;
  customFactorScore?: number;
  environmentMultiplier: number;
  canvasZone: CanvasZone;

  // Domino of the Day (Leverage & Blast Radius)
  dominoOfDay?: boolean;
  dominoLeverageScore?: number;
  unblockedTasksCount?: number;
  unblockedEngineersCount?: number;
  releasedGravityPoints?: number;

  // Feature 3: Entropy Decay Detector & Zombie Defibrillator
  entropyScore?: number;
  entropyLevel?: 'FRESH' | 'STAGNATING' | 'ZOMBIE_BLOCKER';
  defibrillatorAvailable?: boolean;
}

export interface DefibrillateTaskRequest {
  action: 'SPLIT' | 'SHED_GRAVITY' | 'SMART_PASS' | 'DISSOLVE';
  targetAssigneeId?: number;
}

export interface CreateTaskRequest {
  title: string;
  description: string;
  environment: TaskEnvironment;
  customFactorApplied?: boolean;
  assigneeId?: number;
  deadline: string;
  parentTaskIds?: number[];
  overrideCircuitBreaker?: boolean;
}

export interface ReassignTaskRequest {
  assigneeId: number | null;
  overrideCircuitBreaker?: boolean;
}

