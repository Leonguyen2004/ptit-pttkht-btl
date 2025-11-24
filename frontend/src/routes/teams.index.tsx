import { createFileRoute } from '@tanstack/react-router';
import { ManageTeamView } from '../components/ManageTeamView';

export const Route = createFileRoute('/teams/')({
  component: ManageTeamView,
});
