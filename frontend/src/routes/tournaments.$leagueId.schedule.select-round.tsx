import { createFileRoute } from '@tanstack/react-router';
import { SearchRoundView } from '../components/SearchRoundView';

export const Route = createFileRoute('/tournaments/$leagueId/schedule/select-round')({
  component: SelectRoundPage,
});

function SelectRoundPage() {
  const { leagueId } = Route.useParams();
  return <SearchRoundView leagueId={leagueId} />;
}
