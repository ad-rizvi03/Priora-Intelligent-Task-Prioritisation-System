import { Injectable, signal, computed } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { User } from '../models/user.model';

/**
 * Authentication Service managing Security Context, Credentials, and Roles
 * (Project Manager, Team Lead, Developer, Tester).
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8081/api';

  public currentUser = signal<User | null>(null);
  public isAuthenticated = computed(() => this.currentUser() !== null);
  
  // Specific Role Indicators
  public isProjectManager = computed(() => this.currentUser()?.role === 'ROLE_PROJECT_MANAGER');
  public isTeamLead = computed(() => this.currentUser()?.role === 'ROLE_TEAM_LEAD');
  public isTester = computed(() => this.currentUser()?.role === 'ROLE_TESTER');
  public isDeveloper = computed(() => this.currentUser()?.role === 'ROLE_TEAM_MEMBER');

  // Combined Lead Authority (Project Managers and Team Leads)
  public hasLeadPermissions = computed(() => 
    this.currentUser()?.role === 'ROLE_PROJECT_MANAGER' || 
    this.currentUser()?.role === 'ROLE_TEAM_LEAD'
  );

  // Backward-compatibility aliases
  public isAdmin = computed(() => this.isProjectManager());
  public isManager = computed(() => this.hasLeadPermissions());

  private activeCredentials = signal<{ user: string; pass: string } | null>(null);

  constructor(private http: HttpClient) {
    const saved = localStorage.getItem('gravity_auth');
    if (saved) {
      try {
        const creds = JSON.parse(saved);
        this.activeCredentials.set(creds);
        this.loadCurrentUser();
      } catch (e) {
        this.logout();
      }
    } else {
      this.loadCurrentUser();
    }
  }

  public getHeaders(): HttpHeaders {
    const creds = this.activeCredentials();
    if (!creds) return new HttpHeaders();
    const token = btoa(`${creds.user}:${creds.pass}`);
    return new HttpHeaders({
      'Authorization': `Basic ${token}`,
      'Content-Type': 'application/json'
    });
  }

  public login(username: string, pass: string): Observable<User> {
    const token = btoa(`${username}:${pass}`);
    const headers = new HttpHeaders({
      'Authorization': `Basic ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.get<User>(`${this.apiUrl}/auth/me`, { headers }).pipe(
      tap((user) => {
        this.activeCredentials.set({ user: username, pass });
        this.currentUser.set(user);
        localStorage.setItem('gravity_auth', JSON.stringify({ user: username, pass }));
      }),
      catchError((err) => {
        this.logout();
        return throwError(() => err);
      })
    );
  }

  public loadCurrentUser(): void {
    const headers = this.getHeaders();
    if (!headers.has('Authorization')) return;

    this.http.get<User>(`${this.apiUrl}/auth/me`, { headers }).subscribe({
      next: (user) => this.currentUser.set(user),
      error: () => this.logout()
    });
  }

  public updatePasswordInSession(newPassword: string): void {
    const creds = this.activeCredentials();
    if (creds) {
      const updated = { user: creds.user, pass: newPassword };
      this.activeCredentials.set(updated);
      localStorage.setItem('gravity_auth', JSON.stringify(updated));
    }
  }

  public logout(): void {
    this.currentUser.set(null);
    this.activeCredentials.set(null);
    localStorage.removeItem('gravity_auth');
  }
}
