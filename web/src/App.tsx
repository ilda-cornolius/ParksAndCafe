import { useEffect, useMemo, useState } from "react";
import {
  GoogleAuthProvider,
  onAuthStateChanged,
  signInWithPopup,
  signOut,
  type User,
} from "firebase/auth";
import { auth, isFirebaseConfigured } from "./firebase";
import { deletePlace, subscribePlaces, updatePlace } from "./placesApi";
import type { Place, PlaceFilter } from "./types";

function mapsUrl(lat: number, lng: number) {
  return `https://www.google.com/maps?q=${lat},${lng}`;
}

function formatWhen(date: Date | null) {
  if (!date) return "Unknown time";
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export default function App() {
  const [user, setUser] = useState<User | null>(null);
  const [places, setPlaces] = useState<Place[]>([]);
  const [filter, setFilter] = useState<PlaceFilter>("all");
  const [error, setError] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [draftName, setDraftName] = useState("");
  const [draftNotes, setDraftNotes] = useState("");

  useEffect(() => {
    if (!auth) return;
    return onAuthStateChanged(auth, setUser);
  }, []);

  useEffect(() => {
    if (!user) {
      setPlaces([]);
      return;
    }
    return subscribePlaces(
      user.uid,
      (next) => {
        setPlaces(next);
        setError(null);
      },
      (err) => setError(err.message),
    );
  }, [user]);

  const visible = useMemo(() => {
    if (filter === "all") return places;
    return places.filter((p) => p.type === filter);
  }, [places, filter]);

  async function handleSignIn() {
    if (!auth) return;
    try {
      await signInWithPopup(auth, new GoogleAuthProvider());
    } catch (e) {
      setError(e instanceof Error ? e.message : "Sign-in failed");
    }
  }

  async function handleSignOut() {
    if (!auth) return;
    await signOut(auth);
  }

  function startEdit(place: Place) {
    setEditingId(place.id);
    setDraftName(place.name ?? "");
    setDraftNotes(place.notes ?? "");
  }

  async function saveEdit(id: string) {
    await updatePlace(id, { name: draftName.trim(), notes: draftNotes.trim() });
    setEditingId(null);
  }

  return (
    <div className="app">
      <header className="hero">
        <h1 className="brand">Parks &amp; Cafe</h1>
        <p className="tagline">
          Spots you saved from the road — parks in green, cafes in coffee brown.
          Filter, rename, and open in Maps.
        </p>
      </header>

      {!isFirebaseConfigured && (
        <p className="setup">
          Firebase is not configured yet. Copy <code>web/.env.example</code> to{" "}
          <code>web/.env</code>, paste your web app keys from the Firebase
          Console, then restart <code>npm run dev</code>.
        </p>
      )}

      {isFirebaseConfigured && (
        <div className="toolbar">
          <div className="filters" role="group" aria-label="Filter places">
            {(["all", "park", "cafe"] as const).map((value) => (
              <button
                key={value}
                type="button"
                className="filter"
                aria-pressed={filter === value}
                onClick={() => setFilter(value)}
              >
                {value === "all" ? "All" : value === "park" ? "Parks" : "Cafes"}
              </button>
            ))}
          </div>

          {user ? (
            <>
              <span className="user-chip">{user.email}</span>
              <button type="button" className="btn btn-ghost" onClick={handleSignOut}>
                Sign out
              </button>
            </>
          ) : (
            <button type="button" className="btn btn-primary" onClick={handleSignIn}>
              Sign in with Google
            </button>
          )}
        </div>
      )}

      <main className="panel">
        {error && <p className="error">{error}</p>}

        {isFirebaseConfigured && !user && (
          <p className="empty">Sign in to see places saved from your phone widget.</p>
        )}

        {user && visible.length === 0 && !error && (
          <p className="empty">
            No places yet. Add the Parks &amp; Cafe widget on Android and tap Park
            or Cafe.
          </p>
        )}

        {user && visible.length > 0 && (
          <ul className="list">
            {visible.map((place) => (
              <li key={place.id} className="place">
                <span className={`badge ${place.type}`}>{place.type}</span>
                <div>
                  <h2 className="place-title">
                    {place.name?.trim() ||
                      `${place.lat.toFixed(5)}, ${place.lng.toFixed(5)}`}
                  </h2>
                  <p className="meta">
                    {formatWhen(place.createdAt)} ·{" "}
                    <a href={mapsUrl(place.lat, place.lng)} target="_blank" rel="noreferrer">
                      Open in Maps
                    </a>
                  </p>
                  {place.notes?.trim() && editingId !== place.id && (
                    <p className="notes">{place.notes}</p>
                  )}
                  {editingId === place.id && (
                    <div className="edit-form">
                      <input
                        value={draftName}
                        onChange={(e) => setDraftName(e.target.value)}
                        placeholder="Name"
                        aria-label="Place name"
                      />
                      <textarea
                        value={draftNotes}
                        onChange={(e) => setDraftNotes(e.target.value)}
                        placeholder="Notes"
                        rows={3}
                        aria-label="Notes"
                      />
                      <div className="edit-actions">
                        <button
                          type="button"
                          className="btn btn-primary"
                          onClick={() => void saveEdit(place.id)}
                        >
                          Save
                        </button>
                        <button
                          type="button"
                          className="btn btn-ghost"
                          onClick={() => setEditingId(null)}
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  )}
                </div>
                <div className="actions">
                  {editingId !== place.id && (
                    <button type="button" onClick={() => startEdit(place)}>
                      Edit
                    </button>
                  )}
                  <button
                    type="button"
                    className="danger"
                    onClick={() => void deletePlace(place.id)}
                  >
                    Delete
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </main>
    </div>
  );
}
