import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { MasterFormComponent } from './components/master-form/master-form.component';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/master', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'master', component: MasterFormComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: '/master' },
];
