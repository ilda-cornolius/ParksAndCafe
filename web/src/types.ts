export type PlaceType = "park" | "cafe";

export type PlaceFilter = "all" | PlaceType;

export interface Place {
  id: string;
  lat: number;
  lng: number;
  type: PlaceType;
  createdAt: Date | null;
  userId: string;
  name?: string | null;
  notes?: string | null;
}
