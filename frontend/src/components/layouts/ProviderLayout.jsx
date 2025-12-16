import { Outlet } from 'react-router-dom';
import ProviderHeader from './ProviderHeader';

export default function ProviderLayout() {
  return (
    <div className="min-h-screen bg-background">
      <ProviderHeader />
      <main className="container py-6">
        <Outlet />
      </main>
    </div>
  );
}