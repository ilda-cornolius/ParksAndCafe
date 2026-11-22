import {
  collection,
  deleteDoc,
  doc,
  onSnapshot,
  orderBy,
  query,
  updateDoc,
  where,
  type Timestamp,
} from "firebase/firestore";
import { db } from "./firebase";
import type { Place, PlaceType } from "./types";

function toDate(value: Timestamp | Date | null | undefined): Date | null {
  if (!value) return null;
  if (value instanceof Date) return value;
  if (typeof (value as Timestamp).toDate === "function") {
    return (value as Timestamp).toDate();
  }
  return null;
}

export function subscribePlaces(
  userId: string,
  onData: (places: Place[]) => void,
  onError: (error: Error) => void,
): () => void {
  if (!db) {
    onError(new Error("Firebase is not configured"));
    return () => undefined;
  }

  const q = query(
    collection(db, "places"),
    where("userId", "==", userId),
    orderBy("createdAt", "desc"),
  );

  return onSnapshot(
    q,
    (snap) => {
      const places: Place[] = snap.docs.map((d) => {
        const data = d.data();
        return {
          id: d.id,
          lat: Number(data.lat),
          lng: Number(data.lng),
          type: data.type as PlaceType,
          createdAt: toDate(data.createdAt),
          userId: String(data.userId),
          name: data.name ?? null,
          notes: data.notes ?? null,
        };
      });
      onData(places);
    },
    (err) => onError(err),
  );
}

export async function updatePlace(
  id: string,
  patch: { name?: string; notes?: string },
): Promise<void> {
  if (!db) throw new Error("Firebase is not configured");
  await updateDoc(doc(db, "places", id), patch);
}

export async function deletePlace(id: string): Promise<void> {
  if (!db) throw new Error("Firebase is not configured");
  await deleteDoc(doc(db, "places", id));
}
