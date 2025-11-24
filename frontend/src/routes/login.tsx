import { createFileRoute } from '@tanstack/react-router';
import { LoginView } from '../components/LoginView';

export const Route = createFileRoute('/login')({
  component: LoginView,
});

