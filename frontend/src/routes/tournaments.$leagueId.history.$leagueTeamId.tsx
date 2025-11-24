import { createFileRoute } from '@tanstack/react-router';
import { HistoryMatchView } from '../components/HistoryMatchView';

export const Route = createFileRoute('/tournaments/$leagueId/history/$leagueTeamId')({
  component: HistoryMatchPage,
});

function HistoryMatchPage() {
  const { leagueId, leagueTeamId } = Route.useParams();
  return <HistoryMatchView leagueId={leagueId} leagueTeamId={leagueTeamId} />;
}
