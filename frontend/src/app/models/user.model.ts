export type UserRole = 'ROLE_PROJECT_MANAGER' | 'ROLE_TEAM_LEAD' | 'ROLE_TEAM_MEMBER' | 'ROLE_TESTER';

export interface User {
  id: number;
  username: string;
  fullName: string;
  role: UserRole | string;
  employeeNo?: string;
  department?: string;

  // Gravitational Load & Burnout Circuit Breaker Metrics
  activeGravityLoad?: number;
  activeTasksCount?: number;
  focusTasksCount?: number;
  capacityPercentage?: number;
  burnoutStatus?: 'NORMAL' | 'HEAVY' | 'SUPERNOVA_LOCKED';
}

export interface CreateUserRequest {
  username: string;
  password: string;
  fullName: string;
  role: string;
  employeeNo: string;
  department: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
