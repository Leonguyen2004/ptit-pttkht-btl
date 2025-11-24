import { createFileRoute } from '@tanstack/react-router';
import { AddMatchView } from '../components/AddMatchView';

export const Route = createFileRoute('/tournaments/$leagueId/schedule/add-match')({
  component: AddMatchPage,
  validateSearch: (search: Record<string, unknown>) => {
    return {
      roundId: (search.roundId as string) || '',
      stadiumId: (search.stadiumId as string) || undefined,
    };
  },
});

function AddMatchPage() {
  const { leagueId } = Route.useParams();
  const { roundId, stadiumId } = Route.useSearch();
  return <AddMatchView leagueId={leagueId} roundId={roundId} stadiumId={stadiumId} />;
}
