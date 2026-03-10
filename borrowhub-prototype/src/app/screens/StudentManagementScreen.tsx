import { useState, useEffect } from "react";
import { Card } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from "../components/ui/dialog";
import { Plus, Edit, Trash2, Upload, GraduationCap, Search } from "lucide-react";
import { toast } from "sonner";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../components/ui/select";

interface Course {
  id: string;
  name: string;
}

interface Student {
  student_id: string;
  course_id: string;
  name: string;
}

export function StudentManagementScreen() {
  const [students, setStudents] = useState<Student[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [isImportDialogOpen, setIsImportDialogOpen] = useState(false);
  const [editingStudent, setEditingStudent] = useState<Student | null>(null);
  
  const [searchQuery, setSearchQuery] = useState("");
  
  const [formData, setFormData] = useState({
    student_id: "",
    name: "",
    course_name: "", // We will map this to course_id
  });

  const [importText, setImportText] = useState("");

  useEffect(() => {
    loadData();
  }, []);

  const loadData = () => {
    const studentsStr = localStorage.getItem("borrowHubStudents");
    const coursesStr = localStorage.getItem("borrowHubCourses");
    
    if (studentsStr) {
      setStudents(JSON.parse(studentsStr));
    }
    
    if (coursesStr) {
      setCourses(JSON.parse(coursesStr));
    } else {
      // Default courses
      const defaultCourses = [
        { id: "c1", name: "BS Computer Science" },
        { id: "c2", name: "BS Information Technology" },
        { id: "c3", name: "BS Information Systems" },
      ];
      setCourses(defaultCourses);
      localStorage.setItem("borrowHubCourses", JSON.stringify(defaultCourses));
    }
  };

  const getCourseName = (course_id: string) => {
    return courses.find(c => c.id === course_id)?.name || "Unknown Course";
  };

  const handleOpenAdd = () => {
    setEditingStudent(null);
    setFormData({ student_id: "", name: "", course_name: "" });
    setIsAddDialogOpen(true);
  };

  const handleOpenEdit = (student: Student) => {
    setEditingStudent(student);
    setFormData({
      student_id: student.student_id,
      name: student.name,
      course_name: getCourseName(student.course_id),
    });
    setIsAddDialogOpen(true);
  };

  const handleSaveStudent = (e: React.FormEvent) => {
    e.preventDefault();
    
    // Find or create course
    let course_id = "";
    const existingCourse = courses.find(c => c.name.toLowerCase() === formData.course_name.trim().toLowerCase());
    
    if (existingCourse) {
      course_id = existingCourse.id;
    } else {
      course_id = `c${Date.now()}`;
      const newCourse = { id: course_id, name: formData.course_name.trim() };
      const updatedCourses = [...courses, newCourse];
      setCourses(updatedCourses);
      localStorage.setItem("borrowHubCourses", JSON.stringify(updatedCourses));
    }

    const studentData: Student = {
      student_id: formData.student_id.trim(),
      name: formData.name.trim(),
      course_id,
    };

    let updatedStudents = [...students];

    if (editingStudent) {
      const index = updatedStudents.findIndex(s => s.student_id === editingStudent.student_id);
      if (index !== -1) {
        // Prevent changing to an ID that already exists for another student
        if (studentData.student_id !== editingStudent.student_id && updatedStudents.find(s => s.student_id === studentData.student_id)) {
           toast.error("Error", { description: "Student Number already exists." });
           return;
        }
        updatedStudents[index] = studentData;
        toast.success("Student Updated", { description: `${studentData.name} has been updated.` });
      }
    } else {
      if (updatedStudents.find(s => s.student_id === studentData.student_id)) {
        toast.error("Error", { description: "Student Number already exists." });
        return;
      }
      updatedStudents.push(studentData);
      toast.success("Student Added", { description: `${studentData.name} has been added.` });
    }

    setStudents(updatedStudents);
    localStorage.setItem("borrowHubStudents", JSON.stringify(updatedStudents));
    setIsAddDialogOpen(false);
  };

  const handleDelete = (student_id: string) => {
    const updatedStudents = students.filter(s => s.student_id !== student_id);
    setStudents(updatedStudents);
    localStorage.setItem("borrowHubStudents", JSON.stringify(updatedStudents));
    toast.success("Student Deleted", { description: `Student ${student_id} has been removed.` });
  };

  const handleImport = () => {
    if (!importText.trim()) return;

    const lines = importText.trim().split("\n");
    let updatedCourses = [...courses];
    let newStudents: Student[] = [];
    let addedCount = 0;
    let skippedCount = 0;

    lines.forEach(line => {
      // Expecting CSV: student_id,name,course_name
      const parts = line.split(",").map(p => p.trim());
      if (parts.length >= 3) {
        const [student_id, name, ...courseParts] = parts;
        const course_name = courseParts.join(",").trim(); // In case course name has commas

        if (student_id && name && course_name) {
          // Check if student exists
          if (students.find(s => s.student_id === student_id) || newStudents.find(s => s.student_id === student_id)) {
            skippedCount++;
            return;
          }

          // Resolve course
          let course_id = "";
          const existingCourse = updatedCourses.find(c => c.name.toLowerCase() === course_name.toLowerCase());
          if (existingCourse) {
            course_id = existingCourse.id;
          } else {
            course_id = `c${Date.now()}_${Math.random().toString(36).substr(2, 5)}`;
            updatedCourses.push({ id: course_id, name: course_name });
          }

          newStudents.push({ student_id, name, course_id });
          addedCount++;
        }
      }
    });

    if (addedCount > 0) {
      setCourses(updatedCourses);
      localStorage.setItem("borrowHubCourses", JSON.stringify(updatedCourses));
      
      const allStudents = [...students, ...newStudents];
      setStudents(allStudents);
      localStorage.setItem("borrowHubStudents", JSON.stringify(allStudents));
      
      toast.success("Import Successful", { description: `Added ${addedCount} students. Skipped ${skippedCount} duplicates.` });
    } else {
      toast.error("Import Failed", { description: "No valid new students found to import." });
    }

    setImportText("");
    setIsImportDialogOpen(false);
  };

  // Pagination & Filtering
  const filteredStudents = students.filter(s => 
    s.student_id.toLowerCase().includes(searchQuery.toLowerCase()) || 
    s.name.toLowerCase().includes(searchQuery.toLowerCase())
  );
  
  // Basic pagination
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;
  const totalPages = Math.ceil(filteredStudents.length / itemsPerPage);
  
  const currentStudents = filteredStudents.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage);

  return (
    <div className="max-w-6xl mx-auto space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2 tracking-tight">Student Management</h1>
          <p className="text-gray-500 text-base">Manage master list of university students</p>
        </div>
        <div className="flex gap-3">
          <Button 
            variant="outline"
            className="border-gray-300 sm:w-auto rounded-xl shadow-sm h-11 px-5 font-medium bg-white hover:bg-gray-50" 
            onClick={() => setIsImportDialogOpen(true)}
          >
            <Upload className="w-4 h-4 mr-2 text-gray-600" />
            Bulk Import
          </Button>
          <Button 
            className="bg-gradient-to-br from-black to-gray-800 hover:from-gray-800 hover:to-gray-900 sm:w-auto rounded-xl shadow-lg h-11 px-5 font-medium" 
            onClick={handleOpenAdd}
          >
            <Plus className="w-4 h-4 mr-2" />
            Add Student
          </Button>
        </div>
      </div>

      <Card className="border-gray-200/60 overflow-hidden shadow-sm rounded-2xl bg-white/80 backdrop-blur-sm">
        <div className="p-4 border-b border-gray-100 flex justify-between items-center">
          <div className="relative w-full max-w-sm">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <Input 
              placeholder="Search by student number or name..." 
              value={searchQuery}
              onChange={(e) => { setSearchQuery(e.target.value); setCurrentPage(1); }}
              className="pl-9 h-10 bg-white border-gray-200 rounded-xl"
            />
          </div>
          <div className="text-sm text-gray-500 font-medium">
            Total Students: {students.length}
          </div>
        </div>
        
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gradient-to-r from-gray-50 to-gray-100 border-b border-gray-200">
              <tr>
                <th className="text-left px-5 py-4 text-sm font-semibold text-gray-700">Student Number</th>
                <th className="text-left px-5 py-4 text-sm font-semibold text-gray-700">Full Name</th>
                <th className="text-left px-5 py-4 text-sm font-semibold text-gray-700">Course</th>
                <th className="text-right px-5 py-4 text-sm font-semibold text-gray-700">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {currentStudents.length > 0 ? (
                currentStudents.map((student) => (
                  <tr key={student.student_id} className="hover:bg-gray-50/80 transition-all duration-200">
                    <td className="px-5 py-4 text-sm font-medium text-gray-900">{student.student_id}</td>
                    <td className="px-5 py-4 text-sm text-gray-600 flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center text-xs font-bold">
                        {student.name.charAt(0)}
                      </div>
                      {student.name}
                    </td>
                    <td className="px-5 py-4">
                      <span className="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-medium bg-blue-50 text-blue-700 border border-blue-100">
                        <GraduationCap className="w-3 h-3 mr-1.5" />
                        {getCourseName(student.course_id)}
                      </span>
                    </td>
                    <td className="px-5 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <Button variant="ghost" size="sm" onClick={() => handleOpenEdit(student)} className="h-8 w-8 p-0 text-blue-600 hover:text-blue-700 hover:bg-blue-50">
                          <Edit className="w-4 h-4" />
                        </Button>
                        <Button variant="ghost" size="sm" onClick={() => handleDelete(student.student_id)} className="h-8 w-8 p-0 text-red-600 hover:text-red-700 hover:bg-red-50">
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={4} className="px-5 py-8 text-center text-gray-500">
                    No students found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        
        {/* Pagination controls */}
        {totalPages > 1 && (
          <div className="p-4 border-t border-gray-100 flex items-center justify-between bg-gray-50/50">
            <span className="text-sm text-gray-500">
              Showing {(currentPage - 1) * itemsPerPage + 1} to {Math.min(currentPage * itemsPerPage, filteredStudents.length)} of {filteredStudents.length} entries
            </span>
            <div className="flex gap-2">
              <Button 
                variant="outline" 
                size="sm" 
                onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                disabled={currentPage === 1}
                className="h-8 px-3 rounded-lg border-gray-200"
              >
                Previous
              </Button>
              <Button 
                variant="outline" 
                size="sm" 
                onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
                className="h-8 px-3 rounded-lg border-gray-200"
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </Card>

      {/* Add/Edit Student Dialog */}
      <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
        <DialogContent className="sm:max-w-[420px] rounded-2xl">
          <DialogHeader>
            <DialogTitle className="text-xl font-bold">{editingStudent ? "Edit Student" : "Add New Student"}</DialogTitle>
            <DialogDescription className="text-sm text-gray-500">
              Provide the student's information below.
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSaveStudent} className="space-y-4 mt-4">
            <div className="space-y-2">
              <Label htmlFor="student_id">Student Number</Label>
              <Input
                id="student_id"
                placeholder="e.g. 2024-12345"
                value={formData.student_id}
                onChange={(e) => setFormData({ ...formData, student_id: e.target.value })}
                className="h-11 rounded-xl"
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="name">Full Name</Label>
              <Input
                id="name"
                placeholder="e.g. Juan Dela Cruz"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="h-11 rounded-xl"
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="course">Course</Label>
              <Select 
                value={formData.course_name} 
                onValueChange={(val) => setFormData({ ...formData, course_name: val })}
                required
              >
                <SelectTrigger className="h-11 rounded-xl bg-white">
                  <SelectValue placeholder="Select a course" />
                </SelectTrigger>
                <SelectContent>
                  {courses.map((course) => (
                    <SelectItem key={course.id} value={course.name}>
                      {course.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="pt-2 flex gap-3">
              <Button type="button" variant="outline" onClick={() => setIsAddDialogOpen(false)} className="flex-1 rounded-xl">
                Cancel
              </Button>
              <Button type="submit" className="flex-1 rounded-xl bg-gray-900 text-white hover:bg-gray-800">
                {editingStudent ? "Save Changes" : "Save Student"}
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>

      {/* Bulk Import Dialog */}
      <Dialog open={isImportDialogOpen} onOpenChange={setIsImportDialogOpen}>
        <DialogContent className="sm:max-w-[500px] rounded-2xl">
          <DialogHeader>
            <DialogTitle className="text-xl font-bold">Bulk Import Students</DialogTitle>
            <DialogDescription className="text-sm text-gray-500">
              Paste CSV data below. Format should be: <br/>
              <code className="text-xs bg-gray-100 px-1 py-0.5 rounded">Student Number, Full Name, Course Name</code>
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 mt-4">
            <div className="space-y-2">
              <Label>CSV Data</Label>
              <textarea
                value={importText}
                onChange={(e) => setImportText(e.target.value)}
                placeholder="2024-00001, John Doe, BS Computer Science&#10;2024-00002, Jane Smith, BS Information Technology"
                className="w-full min-h-[200px] p-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-gray-900 font-mono"
              />
            </div>
            <DialogFooter className="flex gap-3 sm:justify-end pt-2">
              <Button type="button" variant="outline" onClick={() => setIsImportDialogOpen(false)} className="rounded-xl">
                Cancel
              </Button>
              <Button type="button" onClick={handleImport} className="rounded-xl bg-gray-900 text-white hover:bg-gray-800">
                <Upload className="w-4 h-4 mr-2" />
                Import Data
              </Button>
            </DialogFooter>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}