# API Verification Guide

This document provides `curl` commands to test the core features of the LJC Game Server, focusing on the Hall System, Battle Logic, and Story Progression (1-1 to 1-10).

## Prerequisites

- Server running on `localhost:8080` (or configured port)
- `curl` installed
- `jq` installed (optional, for pretty printing JSON)
- Database initialized with `data.sql`

## 1. User & Initial State

### 1.1 Check Initial Generals (Admin User ID: 1)
```bash
curl "http://localhost:8080/hall/generals?userId=1"
```
*Expected:* Should return General 1001 (Activated) and 1002 (Not Activated).

### 1.2 Check Initial Progress
```bash
curl "http://localhost:8080/hall/progress?userId=1"
```
*Expected:* Empty list or initial CN record (if initialized).

## 2. Hall System

### 2.1 Recruit Troops
Recruit 50 Infantry (ID: 2001)
```bash
curl -X POST "http://localhost:8080/hall/recruit?userId=1" \
     -H "Content-Type: application/json" \
     -d '{"troopId": 2001, "count": 50}'
```

### 2.2 Activate General
Activate General 1002 (Needs 1000 Gold, user has 100000)
```bash
curl -X POST "http://localhost:8080/hall/general/activate?userId=1&generalId=2"
```
*Note:* `generalId` depends on DB ID. In `data.sql`, user_generals ids are auto-increment. Check DB or response from 1.1. Assuming ID 2 for Template 1002.

### 2.3 Upgrade General
Upgrade General 1 (Template 1001)
```bash
curl -X POST "http://localhost:8080/hall/general/upgrade?userId=1&generalId=1"
```

## 3. Battle System (Story Loop)

### 3.1 Start Battle (Stage 1-1)
*   **Civ:** CN
*   **Stage:** 1
*   **General:** 1 (UserGeneral ID)
*   **Troops:** 10 Infantry (Troop ID 2001)

```bash
curl -X POST "http://localhost:8080/battle/start" \
     -H "Content-Type: application/json" \
     -d '{
       "userId": 1,
       "civ": "CN",
       "stageNo": 1,
       "generalId": 1,
       "troops": {
         "2001": 10
       }
     }'
```
*Response:* Returns `battleId` (e.g., `1707300000000`).

### 3.2 Process Turn (Loop until End)
Replace `1707300000000` with actual `battleId`.
```bash
curl -X POST "http://localhost:8080/battle/action" \
     -H "Content-Type: application/json" \
     -d '{
       "userId": 1,
       "battleId": 1707300000000,
       "castSkill": false
     }'
```
*Check `ended: true` and `win: true` in response.*

### 3.3 Verify Unlock (After Stage 1 Win)
Check if General 1002 is unlocked (if not already).
```bash
curl "http://localhost:8080/hall/generals?userId=1"
```

### 3.4 Progress to Stage 1-10 (Simulation)
To test Stage 10 unlock, you can repeat 3.1 and 3.2 for Stage 10 (assuming you cheat or play through 2-9).

**Start Stage 10 (Boss):**
```bash
curl -X POST "http://localhost:8080/battle/start" \
     -H "Content-Type: application/json" \
     -d '{
       "userId": 1,
       "civ": "CN",
       "stageNo": 10,
       "generalId": 1,
       "troops": {
         "2001": 100,
         "2003": 50
       }
     }'
```

After winning Stage 10, check for **Next Country Unlock**:
```bash
curl "http://localhost:8080/hall/progress?userId=1"
```
*Expected:* CN Max Stage 10, JP Unlocked.

## 4. Other Features

### 4.1 Equipment Enhance
Enhance Equipment 1
```bash
curl -X POST "http://localhost:8080/hall/equipment/enhance?userId=1&equipmentId=1"
```

### 4.2 Gem Inlay
Inlay Gem 1 into Equipment 1 Slot 1
```bash
curl -X POST "http://localhost:8080/hall/gem/inlay" \
     -H "Content-Type: application/json" \
     -d '{"equipmentId": 1, "socketIndex": 1, "gemId": 1}'
```

## 5. Clean Reset
To reset data for fresh test:
Restart server (reloads `data.sql`)
OR run:
```sql
DELETE FROM battle_session;
DELETE FROM user_generals WHERE id > 2;
UPDATE user_civ_progress SET max_stage_cleared=0 WHERE user_id=1;
```
