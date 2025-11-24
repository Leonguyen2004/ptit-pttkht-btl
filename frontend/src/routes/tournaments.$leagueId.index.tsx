import { createFileRoute } from '@tanstack/react-router';
import { ManageLeagueView } from '../components/ManageLeagueView';

export const Route = createFileRoute('/tournaments/$leagueId/')({
  component: TournamentDetailPage,
});

function TournamentDetailPage() {
  const { leagueId } = Route.useParams();
  return <ManageLeagueView leagueId={leagueId} />;
}
