import { createFileRoute } from '@tanstack/react-router';
import { RankingView } from '../components/RankingView';

export const Route = createFileRoute('/tournaments/$leagueId/ranking')({
  component: RankingPage,
});

function RankingPage() {
  const { leagueId } = Route.useParams();
  return <RankingView leagueId={leagueId} />;
}
