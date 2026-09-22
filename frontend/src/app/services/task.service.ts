import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { Task, CreateTaskRequest, TaskStatus } from '../models/task.model';
import { User } from '../models/user.model';
import { AuthService } from './auth.service';

/**
 * Task Management Service powered by Angular 16 Signals.
 * Manages canvas zone computations, modal states (Deadline, Reassign), and Developer Notification Streams.
 */
@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private apiUrl = 'http://localhost:8081/api';

  // --- ANGULAR 16 SIGNALS STATE ---
  public tasks = signal<Task[]>([]);
  public pendingTasks = signal<Task[]>([]);
  public users = signal<User[]>([]);
  public loading = signal<boolean>(false);
  public errorMessage = signal<string | null>(null);

  // Modal Signals for Deadline & Reassign Modals
  public selectedTaskForDeadline = signal<Task | null>(null);
  public selectedTaskForReassign = signal<Task | null>(null);

  // --- COMPUTED SIGNALS FOR 3 CANVAS ZONES ---
  public focusZoneTasks = computed(() => 
    this.tasks().filter(t => t.canvasZone === 'FOCUS')
  );

  public driftingZoneTasks = computed(() => 
    this.tasks().filter(t => t.canvasZone === 'DRIFTING')
  );

  public orbitZoneTasks = computed(() => 
    this.tasks().filter(t => t.canvasZone === 'ORBIT')
  );

  // Developer Notifications Stream: Pinged tasks assigned to current developer
  public developerNotifications = computed(() => {
    const currentUsername = this.authService.currentUser()?.username;
    if (!currentUsername || this.authService.isManager()) return [];
    return this.tasks().filter(t => t.pinged && t.assignee?.username === currentUsername);
  });

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  public loadTasks(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.http.get<Task[]>(`${this.apiUrl}/tasks`, { headers: this.authService.getHeaders() }).subscribe({
      next: (data) => {
        this.tasks.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to load tasks:', err);
        this.errorMessage.set('Failed to connect to backend server. Make sure Spring Boot is running on port 8081.');
        this.loading.set(false);
      }
    });
  }

  public loadUsers(): void {
    this.http.get<User[]>(`${this.apiUrl}/users`, { headers: this.authService.getHeaders() }).subscribe({
      next: (data) => this.users.set(data),
      error: (err) => console.error('Failed to load users:', err)
    });
  }

  // --- MODAL CONTROLLERS ---
  public openDeadlineModal(task: Task): void {
    this.selectedTaskForDeadline.set(task);
  }

  public closeDeadlineModal(): void {
    this.selectedTaskForDeadline.set(null);
  }

  public openReassignModal(task: Task): void {
    this.selectedTaskForReassign.set(task);
  }

  public closeReassignModal(): void {
    this.selectedTaskForReassign.set(null);
  }

  // --- TASK API OPERATIONS ---
  public createTask(request: CreateTaskRequest): Observable<Task> {
    return this.http.post<Task>(`${this.apiUrl}/tasks`, request, { headers: this.authService.getHeaders() }).pipe(
      tap(() => this.loadTasks()),
      catchError(err => this.handleError(err, 'Failed to create task card. Manager authorization required.'))
    );
  }

  public deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/tasks/${id}`, { headers: this.authService.getHeaders() }).pipe(
      tap(() => this.loadTasks()),
      catchError(err => this.handleError(err, '403 Forbidden: Only Managers can delete tasks.'))
    );
  }

  public updateDeadline(id: number, deadlineIsoString: string): Observable<Task> {
    return this.http.put<Task>(
      `${this.apiUrl}/tasks/${id}/deadline`,
      { deadline: deadlineIsoString },
      { headers: this.authService.getHeaders() }
    ).pipe(
      tap(() => {
        this.closeDeadlineModal();
        this.loadTasks();
      }),
      catchError(err => this.handleError(err, '403 Forbidden: Only Managers can edit deadlines.'))
    );
  }

  public reassignTask(id: number, assigneeId: number | null): Observable<Task> {
    return this.http.put<Task>(
      `${this.apiUrl}/tasks/${id}/reassign`,
      { assigneeId },
      { headers: this.authService.getHeaders() }
    ).pipe(
      tap(() => {
        this.closeReassignModal();
        this.loadTasks();
      }),
      catchError(err => this.handleError(err, '403 Forbidden: Only Managers can reassign tasks.'))
    );
  }

  public updateStatus(id: number, status: TaskStatus): Observable<Task> {
    return this.http.put<Task>(
      `${this.apiUrl}/tasks/${id}/status`,
      { status },
      { headers: this.authService.getHeaders() }
    ).pipe(
      tap(() => this.loadTasks()),
      catchError(err => this.handleError(err, 'Failed to update task status.'))
    );
  }

  public pingBlocker(id: number): Observable<Task> {
    return this.http.post<Task>(
      `${this.apiUrl}/tasks/${id}/ping`,
      {},
      { headers: this.authService.getHeaders() }
    ).pipe(
      tap(() => this.loadTasks()),
      catchError(err => this.handleError(err, 'Failed to ping task blocker.'))
    );
  }

  private handleError(err: any, defaultMsg: string): Observable<never> {
    if (err.status === 403) {
      this.errorMessage.set('Access Denied (403 Forbidden): Manager authorization required.');
    } else {
      this.errorMessage.set(defaultMsg);
    }
    return throwError(() => err);
  }
}
