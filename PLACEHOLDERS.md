### 1. Placeholder List

#### **Dynamic Placeholders**
*These adapt to the situation: If a KoTH is running, they show that event. If not, they show information about the next scheduled event.*
*   `%minekoth_koth_dynamic_name%`: Name of the current or next KoTH.
*   `%minekoth_koth_dynamic_time%`: Time left (if running) or time until start (if scheduled).
*   `%minekoth_koth_dynamic_location%`: Coordinates (X, Z) of the current or next event.
*   `%minekoth_koth_dynamic_state%`: Current state (e.g., "Capturing", "Stalemate") or "Not Started".

#### **Current Event Placeholders**
*These only return data if a KoTH is currently active. Otherwise, they return empty.*
*   `%minekoth_koth_current_name%`: Name of the active KoTH.
*   `%minekoth_koth_current_time%`: Time left for the active event.
*   `%minekoth_koth_current_location%`: Location of the active event.
*   `%minekoth_koth_current_state%`: State of the active event.

#### **Global Status Placeholders**
*   `%minekoth_koth_isrunning%`: Returns "true" if any KoTH is currently running, otherwise "false".

#### **Active Gameplay Placeholders**
*These provide specific data regarding the gameplay of the active event.*
*   `%minekoth_koth_capturer%`: Name of the player/team currently capturing.
*   `%minekoth_koth_top_player%`: Name of the player currently in the lead.
*   `%minekoth_koth_capturing_players%`: List of players currently inside the capture zone.
*   `%minekoth_koth_winner%`: Winner(s) of the event (usually displayed right after it ends).
*   `%minekoth_koth_time_since_end%`: Time elapsed since the last event ended.
*   `%minekoth_koth_is_stalemate%`: "True" or "False" depending on stalemate status.
*   `%minekoth_koth_capturetime%`: The capture progress of the requesting player/team.
*   `%minekoth_koth_capturetime_<position>%`: The capture progress of the player at the specific rank position (e.g., `_1`).
*   `%minekoth_koth_top_position%`: The requesting player's current rank position in the event.
*   `%minekoth_koth_playername_<position>%`: The name of the player at the specific rank position.

#### **Legacy Placeholders (Deprecated)**
*These are kept for backward compatibility with your old configuration.*
*   `%minekoth_koth_name%` -> Redirects to `dynamic_name`.
*   `%minekoth_koth_time%` -> Redirects to `dynamic_time`.
*   `%minekoth_koth_location%` -> Redirects to `dynamic_location`.
*   `%minekoth_koth_state%` -> Redirects to `dynamic_state`.
*   `%minekoth_koth_position%` -> Redirects to `top_position`.
*   *(Note: Old specific lookups like `%minekoth_koth_name_<id>%` still work)*.