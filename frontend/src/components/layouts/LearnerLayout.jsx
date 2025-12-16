import { Outlet } from 'react-router-dom';
import LearnerHeader from './LearnerHeader';

export default function LearnerLayout() {
  return (
    <div className="min-h-screen bg-background">
      <LearnerHeader />
      <main className="container py-6">
        <Outlet />
      </main>
    </div>
  );
}