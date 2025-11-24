import { createFileRoute } from '@tanstack/react-router';
import { AddStadiumView } from '../components/AddStadiumView';

export const Route = createFileRoute('/stadiums/add')({
  component: AddStadiumPage,
  validateSearch: (search: Record<string, unknown>) => {
    return {
      returnUrl: (search.returnUrl as string) || undefined,
    };
  },
});

function AddStadiumPage() {
  const { returnUrl } = Route.useSearch();
  return <AddStadiumView returnUrl={returnUrl} />;
}
