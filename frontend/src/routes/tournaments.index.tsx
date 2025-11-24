import { createFileRoute } from '@tanstack/react-router';
import { SearchLeagueView } from '../components/SearchLeagueView';

export const Route = createFileRoute('/tournaments/')({
  component: SearchLeagueView,
});
