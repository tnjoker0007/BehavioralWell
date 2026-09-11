import sqlite3
import json
import os
from datetime import datetime

def export_all_app_data():
    db_path = "behavioral_well.db"
    if not os.path.exists(db_path):
        print(f"Error: {db_path} not found.")
        return

    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()

    cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
    tables = [row['name'] for row in cursor.fetchall() if not row['name'].startswith('sqlite_')]

    exported_data = {
        "export_timestamp": datetime.utcnow().isoformat() + "Z",
        "database": db_path,
        "tables": {}
    }

    for table in tables:
        cursor.execute(f"SELECT * FROM {table};")
        rows = cursor.fetchall()
        rows_list = []
        for row in rows:
            dict_row = dict(row)
            # Parse JSON fields if any
            for k, v in dict_row.items():
                if isinstance(v, str) and (v.startswith('{') or v.startswith('[')):
                    try:
                        dict_row[k] = json.loads(v)
                    except Exception:
                        pass
            rows_list.append(dict_row)
        exported_data["tables"][table] = rows_list

    # Save to JSON file
    json_path = "realtime_app_telemetry_export.json"
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(exported_data, f, indent=2, default=str)
    print(f"[SUCCESS] JSON Export saved to {json_path}")

    # Generate Markdown Summary File
    md_path = "realtime_app_telemetry_report.md"
    with open(md_path, "w", encoding="utf-8") as f:
        f.write(f"# BehavioralWell Real-Time App Telemetry Report\n\n")
        f.write(f"**Export Timestamp:** `{exported_data['export_timestamp']}`\n")
        f.write(f"**Database:** `{db_path}`\n\n")
        
        f.write("## Database Table Summary\n\n")
        f.write("| Table Name | Record Count |\n")
        f.write("|---|---|\n")
        for table, rows in exported_data["tables"].items():
            f.write(f"| `{table}` | {len(rows)} |\n")
        f.write("\n")

        for table, rows in exported_data["tables"].items():
            f.write(f"### Table: `{table}` ({len(rows)} records)\n\n")
            if rows:
                keys = list(rows[0].keys())
                f.write("| " + " | ".join(keys) + " |\n")
                f.write("| " + " | ".join(["---"] * len(keys)) + " |\n")
                for r in rows[-10:]: # Show last 10 records
                    row_vals = [str(r.get(k, '')).replace('\n', ' ') for k in keys]
                    f.write("| " + " | ".join(row_vals) + " |\n")
                f.write("\n")
            else:
                f.write("_No records stored yet._\n\n")

    print(f"[SUCCESS] Markdown Report saved to {md_path}")
    conn.close()

if __name__ == "__main__":
    export_all_app_data()

