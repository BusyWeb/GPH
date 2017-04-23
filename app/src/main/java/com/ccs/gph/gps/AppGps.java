package com.ccs.gph.gps;

import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.support.v4.provider.DocumentFile;

import com.ccs.gph.util.AppShared;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class AppGps {

	public String FolderName;
	public String FileName;
	public File GpsFile;
    public File SummaryFile;
	public ArrayList<Gps> GpsEntries;
    public SessionSummary Summary;
	
	public AppGps() {
		this.GpsEntries = new ArrayList<Gps>();
        this.Summary = new SessionSummary();
	}
	
	public AppGps(String folderName, String fileName) throws IOException {
		this.FolderName = folderName;
		this.FileName = fileName;
		this.GpsFile = new File((folderName.endsWith("/") ? folderName : folderName + "/") + fileName);
        this.SummaryFile = new File((folderName.endsWith("/") ? folderName : folderName + "/") + fileName + ".summary");
		this.GpsEntries = new ArrayList<Gps>();
        this.Summary = new SessionSummary();

        if (!SummaryFile.exists()) {
            //SummaryFile.createNewFile();
        }
	}

	public AppGps(File file) {
		this.GpsFile = file;
		this.FileName = file.getName();
		this.FolderName = file.getAbsolutePath().replace(this.FileName, "");
        this.SummaryFile = new File(file.getAbsolutePath() + ".summary");
		this.GpsEntries = new ArrayList<Gps>();
        this.Summary = new SessionSummary();
	}
	
	public void ClearGpsEntries() {
		if (GpsEntries != null) {
			this.GpsEntries.clear();
		}
		this.Summary = new SessionSummary();
	}

	public void InitSummary(String id, String userId, Long startTime, Double fuelCost, Boolean hasVideo, Boolean hasObdData) {
        if (this.Summary == null) {
            this.Summary = new SessionSummary(id, userId, startTime, fuelCost, hasVideo, hasObdData);
        } else {
            this.Summary.Id = id;
            this.Summary.UserId = userId;
            this.Summary.StartTime = startTime;
            this.Summary.FuelCost = fuelCost;
            this.Summary.HasVideo = hasVideo;
            this.Summary.HasObdData = hasObdData;
        }
    }

	public void AddGpsEntry(Gps gps) {
		// id fail safe...
		gps.id = this.GpsEntries.size() + 1;
		this.GpsEntries.add(gps);
	}

	public void AddGpsEntry(String data) {
        this.GpsEntries.add(GetGpsInstance(data));
    }

	public Gps GetGpsInstance(int id) {
        return new Gps(id);
    }

    public Gps GetGpsInstance(String data) {
        return new Gps(data);
    }
	
	public Gps GetGpsEntry_A(int id) {
		Gps retValue = null;
		try {
			for (Gps gps : this.GpsEntries) {
				if (gps.id == id) {
					retValue = gps;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retValue;
	}
	
	public Gps GetGpsEntry(int id) {
		Gps retValue = null;
		try {
            if (GpsEntries.size() == 0) {
                return retValue;
            }
			if ((id - 1) < 0 || (id - 1) >= this.GpsEntries.size()) {
				retValue = this.GpsEntries.get(0);
			} else {
				retValue = this.GpsEntries.get(id - 1);
			}
			if (retValue == null && id < this.GpsEntries.size()) {
				retValue = this.GpsEntries.get(id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retValue;
	}

	public void UpdateSummary() {
        try {
            if (GpsEntries == null || GpsEntries.size() == 0) {
                return;
            }

            int count = GpsEntries.size();
            this.Summary.EndTime = GpsEntries.get(count - 1).time;
            float max = 0;
            float total = 0;
            float totalCount = 0;
            for (Gps gps : this.GpsEntries) {
                if (gps.speed > max) {
					max = gps.speed;
				}
                if (gps.speed > 0) {
                    totalCount += 1;
                    total += gps.speed;
                }
            }
            if (totalCount > 0 && total > 0) {
                this.Summary.AvgSpeed = (total / totalCount);
            } else {
                this.Summary.AvgSpeed = 0f;
            }
            this.Summary.MaxSpeed = max;

            if (this.Summary.Distance > 0 && this.Summary.FuelUsed == 0 && this.Summary.FuelCost > 0) {
                //double kilometer = this.Summary.Distance / 1000;
                //this.Summary.FuelUsed = kilometer / AppShared.FuelDistancePerUnit;	//this.Summary.FuelCost;
            }

        } catch (Exception e) {
        }
    }

	public boolean LoadFromFile(File file) {
		boolean success = false;
		FileInputStream in = null;
		BufferedReader reader = null;
		try {
			this.GpsFile = file;
			this.FileName = file.getName();
			this.FolderName = file.getAbsolutePath().replace(this.FileName, "");

			if (GpsEntries == null) {
				GpsEntries = new ArrayList<Gps>();
			}

			ClearGpsEntries();
			
			in = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(in));
			String line;
			
			// read file name
			line = reader.readLine();
			
			while ((line = reader.readLine()) != null) {
				if (line != null && line.length() > 0) {
					GpsEntries.add(new Gps(line));
				}
			}

			if (reader != null) {
				reader.close();
			}
			if (in != null) {
				in.close();
			}

			this.SummaryFile = new File(file.getAbsolutePath() + ".summary");
            if (this.SummaryFile.exists()) {
                in = new FileInputStream(this.SummaryFile);
                reader = new BufferedReader(new InputStreamReader(in));
                line = reader.readLine();
                this.Summary = new SessionSummary(line);

                if (reader != null) {
                    reader.close();
                }
                if (in != null) {
                    in.close();
                }
            }

			success = true;
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		} finally {
		}
		return success;
	}
	
	public boolean SaveToFile(File file) {
		boolean success = false;
		BufferedOutputStream out = null;
		try {
			File f = null;
			if (file == null) {
				f = new File(this.FolderName + this.FileName);
			} else {
				f = file;
			}
			out = new BufferedOutputStream(new FileOutputStream(f));
			
			// write gpx headers
			out.write((f.getAbsolutePath() + "\n").getBytes());
			for (Gps gps : this.GpsEntries) {
				out.write((gps.toString() + "\n").getBytes());
			}
			
			if (out != null) {
				out.flush();
				out.close();
				out = null;
			}

			UpdateSummary();

			if (this.Summary != null) {
                File fileSummary = new File(file.getAbsolutePath() + ".summary");
                out = new BufferedOutputStream(new FileOutputStream(fileSummary));
                out.write((this.Summary.toString() + "\n").getBytes());
                if (out != null) {
                    out.flush();
                    out.close();
                    out = null;
                }
            }

			success = true;
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}

	private String GpxHeaders = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
								+ "<gpx version=\"1.1\" "
								+ "creator=\"Dashboard Cam Android\" "
								+ "xmlns=\"http://www.topografix.com/GPX/1/1\" "
								+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
								+ "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" "
								+ ">" ;
	private String GpxFooters = "</trk></gpx>";

	
	public boolean ExportToGpxFile() {
		boolean success = false;
		BufferedOutputStream out = null;
		try {
			File f = new File(this.FolderName + this.FileName + ".trkpt.gpx");
			out = new BufferedOutputStream(new FileOutputStream(f));
			
			// write headers
			out.write(GpxHeaders.getBytes());
			// write gpx name
			out.write(("<name>" + this.FileName + "</name>\n").getBytes());
			// write gpx track name
			out.write(("<trk><name>" + this.FileName + "</name><number>1</number><trkseg>\n").getBytes());
			// write gpx track points
			for (Gps gps : this.GpsEntries) {
				out.write((gps.toGpxTrkptString() + "\n").getBytes());
			}
			// write footers
			out.write(("</trkseg></trk></gpx>").getBytes());
			if (out != null) {
				out.flush();
				out.close();
				out = null;
			}
			
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}
	public boolean ExportToGpxWayPointFile() {
		boolean success = false;
		BufferedOutputStream out = null;
		try {
			File f = new File(this.FolderName + this.FileName + ".wpt.gpx");
			out = new BufferedOutputStream(new FileOutputStream(f));
			
			// write headers
			out.write(GpxHeaders.getBytes());
			// write gpx name
			out.write(("<metadata><name>" + this.FileName + "</name></metadata>\n").getBytes());

			// write gpx way points
			for (Gps gps : this.GpsEntries) {
				out.write((gps.toGpxWptString() + "\n").getBytes());
			}
			// write footers
			out.write(("</gpx>").getBytes());
			if (out != null) {
				out.flush();
				out.close();
				out = null;
			}
			
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}
	
	public Gps GetGpsEntryFromData(String data) {
		return new Gps(data);
	}

	public class SessionSummary {
        public String Id = "";
        public String UserId = "";
        public Long StartTime = 0L;
        public Long EndTime = 0L;
        public Double Distance = 0.0;       // metric (meters default)
        public Double FuelUsed = 0.0;
        public Double FuelCost = 0.0;       // converted to liter (if user settings use gallon), FuelUsed * 3.7854 = gallon
        public Integer VehicleEvents = 0;
        public Integer HardStop = 0;
        public Integer HardAcceleration = 0;
        public Boolean HasVideo = false;
        public Boolean HasObdData = false;
        public Float MaxSpeed = 0f;
        public Float AvgSpeed = 0f;

		public SessionSummary() {}

        public SessionSummary(String id, String userId, Long startTime, Double fuelCost, Boolean hasVideo, Boolean hasObdData) {
            this.Id = id;
            this.UserId = userId;
            this.StartTime = startTime;
            this.FuelCost = fuelCost;
            this.HasVideo = hasVideo;
            this.HasObdData = hasObdData;
        }

        public SessionSummary(String summary){
            try {
                String[] data = summary.split(",");
                this.Id = data[0];
                this.UserId = data[1];
                this.StartTime = Long.parseLong(data[2]);
                this.EndTime = Long.parseLong(data[3]);
                this.Distance = Double.parseDouble(data[4]);
                this.FuelUsed = Double.parseDouble(data[5]);
                this.FuelCost = Double.parseDouble(data[6]);
                this.VehicleEvents = Integer.parseInt(data[7]);
                this.HardStop = Integer.parseInt(data[8]);
                this.HardAcceleration = Integer.parseInt(data[9]);
                this.HasVideo = Boolean.parseBoolean(data[10]);
                this.HasObdData = Boolean.parseBoolean(data[11]);
                float max = Float.parseFloat(data[12]);
                if (Float.isNaN(max) || Float.isInfinite(max)) {
                    this.MaxSpeed = 0f;
                } else {
                    this.MaxSpeed = max;
                }
                float avg = Float.parseFloat(data[13]);
                if (Float.isNaN(avg) || Float.isInfinite(avg)) {
                    this.AvgSpeed = 0f;
                } else {
                    this.AvgSpeed = avg;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void AddHardStop() {
            this.HardStop += 1;
        }
        public void AddHardAcceleration() {
            this.HardAcceleration += 1;
        }
        public void AddVehicleEvent() {
            this.VehicleEvents += 1;
        }
        public void AddDistance(double meters) {
            this.Distance += meters;
        }

        public String toString() {
            String data = "";
            try {
                data += String.valueOf(this.Id) + ",";
                data += String.valueOf(this.UserId) + ",";
                data += String.valueOf(this.StartTime) + ",";
                data += String.valueOf(this.EndTime) + ",";
                data += String.valueOf(this.Distance) + ",";
                data += String.valueOf(this.FuelUsed) + ",";
                data += String.valueOf(this.FuelCost) + ",";
                data += String.valueOf(this.VehicleEvents) + ",";
                data += String.valueOf(this.HardStop) + ",";
                data += String.valueOf(this.HardAcceleration) + ",";
                data += String.valueOf(this.HasVideo) + ",";
                data += String.valueOf(this.HasObdData) + ",";
                data += String.valueOf(this.MaxSpeed) + ",";
                data += String.valueOf(this.AvgSpeed);
            } catch (Exception e) {
                e.printStackTrace();
                data = ",,,,,,,,,,,,,";
            }
            return data;
        }
    }

	public class Gps {
		public Integer id = 0;
		public Long time = 0L;
		public Long timegps = 0L;
		public Double latitude = 0.0;
		public Double longitude = 0.0;
		public Double altitude = 0.0;
		public Float speed = 0f;
		public Float accuracy = 0f;
		public Float bearing = 0f;
		public Float declination = 0f;
//		public Boolean hasAltitude;
//		public Boolean hasSpeed;
//		public Boolean hasAccuracy;
		
		// OBD2 Information
		public Integer obdrpm = 0;
		public Integer obdtemperature = 0;

		// Accelerometer
		public Float dx = 0.0f;
		public Float dy = 0.0f;
		public Float dz = 0.0f;
		public Double g = 0.0;
		public boolean hardstop = false;
		public boolean hardaccel = false;
		public boolean sensorevent = false;

		public Gps() {
		}

		public Gps(int id) {
			this.id = id;
		}
		public Gps(int id, Long time, Long timeGps, Double latitude, Double longitude, Float speed, Float accurracy, Double altitude, Float bearing, Float declination){
			this.id = id;
			this.time = time;
			this.timegps = timeGps;
			this.latitude = latitude;
			this.longitude = longitude;
			this.speed = speed;
			this.accuracy = accurracy;
			this.altitude = altitude;
			this.bearing = bearing;
			this.declination = declination;
			this.obdrpm = 0;
			this.obdtemperature = 0;
			this.dx = 0.0f;
			this.dy = 0.0f;
			this.dz = 0.0f;
			this.g = 0.0;
			this.hardstop = false;
			this.hardaccel = false;
			this.sensorevent = false;
		}
		
		double factor = 1e2;

		public Gps(String gpsData){
			try {
				String[] data = gpsData.split(",");
				this.id = Integer.parseInt(data[0]);
				this.time = Long.parseLong(data[1]);
				this.timegps = Long.parseLong(data[2]);
				this.latitude = Double.parseDouble(data[3]);
				this.longitude = Double.parseDouble(data[4]);
				
				float s = Float.parseFloat(data[5]);
				if (Float.isNaN(s) || Float.isInfinite(s)) {
					//Log.i("DBG", "ID: " + this.id.toString() + ", " + String.valueOf(s));
					if (this.id == 1) {
						this.speed = 0f;
					} else {
						this.speed = AppShared.gAppGps.GetGpsEntry(this.id - 1).speed;
					}
					//Log.i("DBG", "ID 2: " + this.id.toString() + ", " + String.valueOf(this.speed));
				} else {
					this.speed = s;
				}
				
				this.accuracy = Float.parseFloat(data[6]);
				this.altitude = Double.parseDouble(data[7]);
				this.bearing = Float.parseFloat(data[8]);
				this.declination = Float.parseFloat(data[9]);
				
				// OBD 2 
				if (data.length >= 11) {
					this.obdrpm = Integer.parseInt(data[10]);
				}
				if (data.length >= 12) {
					this.obdtemperature = Integer.parseInt(data[11]);
				}

				// Accelerometer
				if (data.length >= 13) {
					this.dx = Float.parseFloat(data[12]);
					this.dy = Float.parseFloat(data[13]);
					this.dz = Float.parseFloat(data[14]);
					this.g = Double.parseDouble(data[15]);
				}
				// vehicle events
				if (data.length >= 17) {
					this.hardstop = Boolean.parseBoolean(data[16]);
					this.hardaccel = Boolean.parseBoolean(data[17]);
					this.sensorevent = Boolean.parseBoolean(data[18]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
		public String toString() {
			String data = "";
			try {
				data += String.valueOf(this.id) + ",";
				data += String.valueOf(this.time) + ",";
				data += String.valueOf(this.timegps) + ",";
				data += String.valueOf(this.latitude) + ",";
				data += String.valueOf(this.longitude) + ",";
				data += String.valueOf(this.speed) + ",";
				data += String.valueOf(this.accuracy) + ",";
				data += String.valueOf(this.altitude) + ",";
				data += String.valueOf(this.bearing) + ",";
				data += String.valueOf(this.declination) + ",";
				data += String.valueOf(this.obdrpm) + ",";
				data += String.valueOf(this.obdtemperature) + ",";
                data += String.valueOf(this.dx) + ",";
                data += String.valueOf(this.dy) + ",";
                data += String.valueOf(this.dz) + ",";
                data += String.valueOf(this.g) + ",";
				data += String.valueOf(this.hardstop) + ",";
				data += String.valueOf(this.hardaccel) + ",";
				data += String.valueOf(this.sensorevent);
			} catch (Exception e) {
				e.printStackTrace();
				data = ",,,,,,,,,,,";
			}
			return data;
		}
		
		public  String toGpxTrkptString() {
			String data = "";
			try {
				data = "<trkpt lat=\"" + String.valueOf(this.latitude) + "\""
					+ " lon=\"" + String.valueOf(this.longitude) + "\">"
					+ "<ele>" + String.valueOf(this.altitude) + "</ele>"
					+ "<time>" +  GetGpxTimeString(this.timegps) + "</time>"
					+ "<speed>" + String.valueOf(this.speed * 0.277777778) + "</speed>"
					+ "<magvar>" + String.valueOf(this.bearing) + "</magvar></trkpt>";
			} catch (Exception e) {
				e.printStackTrace();
				data = "<trkpt lat=\"0\" lon=\"0\">"
					+ "<ele>0</ele>"
					+ "<time>1970-01-01T00:00:00Z</time>"
					+ "<speed>0</speed>"
					+ "<magvar>0</magvar></trkpt>";
			}
			return data;
		}

		public  String toGpxWptString() {
			String data = "";
			try {
				data = "<wpt lat=\"" + String.valueOf(this.latitude) + "\""
					+ " lon=\"" + String.valueOf(this.longitude) + "\">"
					+ "<ele>" + String.valueOf(this.altitude) + "</ele>"
					+ "<time>" +  GetGpxTimeString(this.timegps) + "</time>"
					+ "<magvar>" + String.valueOf(this.bearing) + "</magvar>"
					+ "<name>" + String.valueOf(this.id) + "</name>"
					+ "<link href=\"" + String.valueOf(this.id) + ".jpg\">"
					+ "<type><![CDATA[Picture]]></type>"
					+ "<text>" + String.valueOf(this.id) + ".jpg</text>"
					+ "</link>"
					+"</wpt>";
			} catch (Exception e) {
				e.printStackTrace();
				data = "<wpt lat=\"0\" lon=\"0\">"
					+ "<ele>0</ele>"
					+ "<time>1970-01-01T00:00:00Z</time>"
					+ "<magvar>0</magvar></wpt>";
			}
			return data;
		}

		public String GetGpxTimeString(Long time) {
			String retValue = "";
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				retValue = sdf.format(new Date(time));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return retValue;
		}
	}
	
}
