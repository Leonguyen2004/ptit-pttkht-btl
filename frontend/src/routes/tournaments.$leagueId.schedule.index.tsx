import { createFileRoute } from '@tanstack/react-router';
import { ManageScheduleView } from '../components/ManageScheduleView';

export const Route = createFileRoute('/tournaments/$leagueId/schedule/')({
  component: SchedulePage,
});

function SchedulePage() {
  const { leagueId } = Route.useParams();
  return <ManageScheduleView leagueId={leagueId} />;
}