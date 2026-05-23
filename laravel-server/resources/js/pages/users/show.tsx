import { Head, useForm } from '@inertiajs/react';
import React from 'react';

type User = {
    id: number;
    name: string;
    email: string;
};

type Props = {
    user: User;
};

export default function Show({ user }: Props) {
    const { data, setData, patch, processing } = useForm({
        name: user.name,
        email: user.email,
    });

    const submit = (e: React.FormEvent) => {
        e.preventDefault();
        patch(`/users/${user.id}`); 
    };

    return (
        <div className="p-6">
            <Head title="User Profile" />

            <h1 className="text-2xl font-bold text-foreground mb-4">
                User Profile
            </h1>
            
            <div className="mb-6 space-y-2 text-foreground">
                <p>ID: {user.id}</p>
                <p>Email: {user.email}</p>
                <p className="font-semibold">Current Name: {user.name}</p>
            </div>

            <form onSubmit={submit} className="flex gap-2 max-w-sm">
                <input
                    type="text"
                    value={data.name}
                    onChange={(e) => setData('name', e.target.value)}
                    placeholder="New name..."
                    className="flex-1 rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                />
                <input
                    type="email"
                    value={data.email}
                    onChange={(e) => setData('email', e.target.value)}
                    placeholder="New email..."
                    className="flex-1 rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                />
                <button 
                    type="submit" 
                    disabled={processing}
                    className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:opacity-50"
                >
                    Update
                </button>
            </form>
        </div>
    );
}