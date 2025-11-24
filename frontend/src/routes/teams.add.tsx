import { createFileRoute } from '@tanstack/react-router';
import { AddTeamView } from '../components/AddTeamView';

export const Route = createFileRoute('/teams/add')({
  component: AddTeamPage,
  validateSearch: (search: Record<string, unknown>) => {
    return {
      stadiumId: (search.stadiumId as string) || undefined,
    };
  },
});

function AddTeamPage() {
  const { stadiumId } = Route.useSearch();
  return <AddTeamView stadiumId={stadiumId} />;
}
