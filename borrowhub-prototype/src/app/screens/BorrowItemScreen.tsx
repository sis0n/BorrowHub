import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import { Card } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../components/ui/select";
import { ArrowLeft, Check } from "lucide-react";

// Item inventory data
const itemCategories = {
  Laptop: Array.from({ length: 30 }, (_, i) => `Laptop ${i + 1}`),
  Projector: Array.from({ length: 5 }, (_, i) => `Projector ${i + 1}`),
  HDMI: Array.from({ length: 5 }, (_, i) => `HDMI Cable ${i + 1}`),
  Camera: ["Camera 1", "Camera 2", "Camera 3"],
  Other: [],
};

export function BorrowItemScreen() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    studentNumber: "",
    studentName: "",
    course: "",
    itemType: "",
    item: "",
    otherItem: "",
  });
  const [isAutoFilled, setIsAutoFilled] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [coursesList, setCoursesList] = useState<{id: string; name: string}[]>([]);

  useEffect(() => {
    const coursesStr = localStorage.getItem("borrowHubCourses");
    if (coursesStr) {
      setCoursesList(JSON.parse(coursesStr));
    }
  }, []);

  // Auto-generated fields
  const currentDateTime = new Date().toLocaleString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
    hour12: true,
  });
  const staffName = "Maria Garcia"; // Would come from logged-in user

  // Handle auto-fill declaratively
  useEffect(() => {
    if (!formData.studentNumber) {
      if (isAutoFilled) {
        setIsAutoFilled(false);
        setFormData(prev => ({ ...prev, studentName: "", course: "" }));
      }
      return;
    }

    try {
      const studentsStr = localStorage.getItem("borrowHubStudents");
      const coursesStr = localStorage.getItem("borrowHubCourses");
      
      if (studentsStr && coursesStr) {
        const students = JSON.parse(studentsStr);
        const courses = JSON.parse(coursesStr);
        
        const foundStudent = students.find((s: any) => s.student_id === formData.studentNumber.trim());
        
        if (foundStudent) {
          let courseName = "";
          const foundCourse = courses.find((c: any) => c.id === foundStudent.course_id);
          if (foundCourse) {
            courseName = foundCourse.name;
          }
          
          setIsAutoFilled(true);
          setFormData(prev => ({
            ...prev,
            studentName: foundStudent.name,
            course: courseName
          }));
        } else if (isAutoFilled) {
          // It was auto-filled, but the number changed to something invalid
          setIsAutoFilled(false);
          setFormData(prev => ({ ...prev, studentName: "", course: "" }));
        }
      }
    } catch (e) {
      console.error("Error parsing student data", e);
    }
  }, [formData.studentNumber]); // Re-run whenever studentNumber changes

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitted(true);
    setTimeout(() => {
      navigate("/app");
    }, 2000);
  };

  const handleChange = (field: string, value: string) => {
    setFormData((prev) => {
      const newData = { ...prev, [field]: value };
      
      // Reset item selection when type changes
      if (field === "itemType") {
        newData.item = "";
        newData.otherItem = "";
      }
      return newData;
    });
  };

  const availableItems = formData.itemType ? itemCategories[formData.itemType as keyof typeof itemCategories] || [] : [];

  if (submitted) {
    return (
      <div className="max-w-2xl mx-auto">
        <Card className="p-8 border-slate-200 text-center">
          <div className="w-16 h-16 bg-green-50 rounded-full flex items-center justify-center mx-auto mb-4">
            <Check className="w-8 h-8 text-green-600" />
          </div>
          <h2 className="text-xl text-slate-900 mb-2">Borrow Request Submitted</h2>
          <p className="text-slate-500 mb-4">The item has been successfully assigned to the borrower.</p>
          <div className="bg-slate-50 rounded-lg p-4 text-sm text-left">
            <div className="text-slate-600 mb-1">Return by end of day:</div>
            <div className="text-slate-900">{new Date().toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}</div>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto">
      {/* Header */}
      <div className="flex items-center gap-3 mb-6">
        <Button
          variant="ghost"
          size="sm"
          onClick={() => navigate("/app")}
          className="text-slate-600 -ml-2"
        >
          <ArrowLeft className="w-4 h-4" />
        </Button>
        <div>
          <h1 className="text-2xl text-slate-900 mb-1">Borrow Item</h1>
          <p className="text-slate-500 text-sm">Create a new borrow request</p>
        </div>
      </div>

      {/* Auto-generated Info */}
      <Card className="p-4 border-slate-200 bg-blue-50 mb-6">
        <div className="space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-slate-600">Date & Time:</span>
            <span className="text-slate-900">{currentDateTime}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-600">Processed by:</span>
            <span className="text-slate-900">{staffName}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-600">Return by:</span>
            <span className="text-slate-900">End of day (Same day return)</span>
          </div>
        </div>
      </Card>

      {/* Form */}
      <Card className="p-6 border-slate-200">
        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Borrower Information Section */}
          <div className="pb-4 border-b border-slate-200">
            <h3 className="text-sm text-slate-600 mb-4">Borrower Information</h3>
            
            {/* Student Number */}
            <div className="space-y-2 mb-4">
              <Label htmlFor="studentNumber" className="text-slate-700">
                Student Number <span className="text-red-500">*</span>
              </Label>
              <Input
                id="studentNumber"
                placeholder="e.g., 2024-12345"
                value={formData.studentNumber}
                onChange={(e) => handleChange("studentNumber", e.target.value)}
                className="h-11 border-slate-300"
                required
              />
            </div>

            {/* Student Name */}
            <div className="space-y-2 mb-4">
              <Label htmlFor="studentName" className="text-slate-700">
                Full Name <span className="text-red-500">*</span>
              </Label>
              <Input
                id="studentName"
                placeholder="e.g., Juan Dela Cruz"
                value={formData.studentName}
                onChange={(e) => handleChange("studentName", e.target.value)}
                className={`h-11 border-slate-300 ${isAutoFilled ? "bg-slate-50 text-slate-500" : ""}`}
                readOnly={isAutoFilled}
                required
              />
            </div>

            {/* Course */}
            <div className="space-y-2">
              <Label htmlFor="course" className="text-slate-700">
                Course <span className="text-red-500">*</span>
              </Label>
              <Select
                value={formData.course}
                onValueChange={(val) => handleChange("course", val)}
                disabled={isAutoFilled}
                required
              >
                <SelectTrigger className={`h-11 border-slate-300 ${isAutoFilled ? "bg-slate-50 text-slate-500" : ""}`}>
                  <SelectValue placeholder="Select a course" />
                </SelectTrigger>
                <SelectContent>
                  {coursesList.map((course) => (
                    <SelectItem key={course.id} value={course.name}>
                      {course.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Item Information Section */}
          <div>
            <h3 className="text-sm text-slate-600 mb-4">Item Information</h3>

            {/* Item Type */}
            <div className="space-y-2 mb-4">
              <Label htmlFor="itemType" className="text-slate-700">
                Item Type <span className="text-red-500">*</span>
              </Label>
              <Select 
                value={formData.itemType} 
                onValueChange={(value) => handleChange("itemType", value)} 
                required
              >
                <SelectTrigger className="h-11 border-slate-300">
                  <SelectValue placeholder="Select item type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Laptop">Laptop</SelectItem>
                  <SelectItem value="Projector">Projector</SelectItem>
                  <SelectItem value="HDMI">HDMI Cable</SelectItem>
                  <SelectItem value="Camera">Camera</SelectItem>
                  <SelectItem value="Other">Other</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Select Specific Item (if not Other) */}
            {formData.itemType && formData.itemType !== "Other" && (
              <div className="space-y-2">
                <Label htmlFor="item" className="text-slate-700">
                  Select Item <span className="text-red-500">*</span>
                </Label>
                <Select 
                  value={formData.item} 
                  onValueChange={(value) => handleChange("item", value)} 
                  required
                >
                  <SelectTrigger className="h-11 border-slate-300">
                    <SelectValue placeholder="Choose specific item" />
                  </SelectTrigger>
                  <SelectContent>
                    {availableItems.map((item) => (
                      <SelectItem key={item} value={item}>
                        {item}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            )}

            {/* Other Item Input */}
            {formData.itemType === "Other" && (
              <div className="space-y-2">
                <Label htmlFor="otherItem" className="text-slate-700">
                  Specify Item <span className="text-red-500">*</span>
                </Label>
                <Input
                  id="otherItem"
                  placeholder="Enter item name"
                  value={formData.otherItem}
                  onChange={(e) => handleChange("otherItem", e.target.value)}
                  className="h-11 border-slate-300"
                  required
                />
              </div>
            )}
          </div>

          {/* Important Note */}
          <div className="bg-amber-50 border border-amber-100 rounded-lg p-4">
            <p className="text-sm text-slate-700">
              <span className="font-medium">Important:</span> All borrowed items must be returned by the end of the day. Same-day return policy applies to all items.
            </p>
          </div>

          {/* Actions */}
          <div className="flex gap-3 pt-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => navigate("/app")}
              className="flex-1 h-11 border-slate-300"
            >
              Cancel
            </Button>
            <Button type="submit" className="flex-1 h-11 bg-slate-800 hover:bg-slate-700">
              Submit Request
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}