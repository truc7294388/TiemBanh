$(document).ready(function() {
    let currentPage = 0;
    const pageSize = 10;
    let totalPages = 0;
    let keyword = '';

    // Initialize Summernote
    $('#content').summernote({
        height: 300,
        toolbar: [
            ['style', ['style']],
            ['font', ['bold', 'underline', 'clear']],
            ['color', ['color']],
            ['para', ['ul', 'ol', 'paragraph']],
            ['table', ['table']],
            ['insert', ['link', 'picture']],
            ['view', ['fullscreen', 'codeview', 'help']]
        ]
    });

    // Load initial data
    loadBlogs();

    // Search functionality
    $('#searchInput').on('keyup', function() {
        keyword = $(this).val();
        currentPage = 0;
        loadBlogs();
    });

    // Pagination
    $('#prevPage').click(function() {
        if (currentPage > 0) {
            currentPage--;
            loadBlogs();
        }
    });

    $('#nextPage').click(function() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadBlogs();
        }
    });

    // Add Blog
    $('#addBlogForm').submit(function(e) {
        e.preventDefault();
        const formData = new FormData();
        const blogData = {
            title: $('#title').val(),
            shortDescription: $('#shortDescription').val(),
            content: $('#content').summernote('code'),
            published: $('#published').is(':checked')
        };
        formData.append('blog', new Blob([JSON.stringify(blogData)], {
            type: 'application/json'
        }));

        const imageFile = $('#image')[0].files[0];
        if (imageFile) {
            formData.append('image', imageFile);
        }

        $.ajax({
            url: '/admin/blogs',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: function(response) {
                $('#addBlogModal').modal('hide');
                showToast('Success', 'Blog created successfully');
                loadBlogs();
                resetForm('#addBlogForm');
            },
            error: function(xhr) {
                showToast('Error', 'Failed to create blog');
            }
        });
    });

    // Edit Blog
    let editId = null;

    $(document).on('click', '.edit-blog', function() {
        editId = $(this).data('id');
        $.get('/admin/blogs/' + editId, function(blog) {
            $('#editTitle').val(blog.title);
            $('#editShortDescription').val(blog.shortDescription);
            $('#editContent').summernote('code', blog.content);
            $('#editPublished').prop('checked', blog.published);
            $('#currentImage').attr('src', blog.imageUrl);
            $('#editBlogModal').modal('show');
        });
    });

    $('#editBlogForm').submit(function(e) {
        e.preventDefault();
        const formData = new FormData();
        const blogData = {
            title: $('#editTitle').val(),
            shortDescription: $('#editShortDescription').val(),
            content: $('#editContent').summernote('code'),
            published: $('#editPublished').is(':checked')
        };
        formData.append('blog', new Blob([JSON.stringify(blogData)], {
            type: 'application/json'
        }));

        const imageFile = $('#editImage')[0].files[0];
        if (imageFile) {
            formData.append('image', imageFile);
        }

        $.ajax({
            url: '/admin/blogs/' + editId,
            type: 'PUT',
            data: formData,
            processData: false,
            contentType: false,
            success: function(response) {
                $('#editBlogModal').modal('hide');
                showToast('Success', 'Blog updated successfully');
                loadBlogs();
                resetForm('#editBlogForm');
            },
            error: function(xhr) {
                showToast('Error', 'Failed to update blog');
            }
        });
    });

    // Delete Blog
    $(document).on('click', '.delete-blog', function() {
        const id = $(this).data('id');
        if (confirm('Are you sure you want to delete this blog?')) {
            $.ajax({
                url: '/admin/blogs/' + id,
                type: 'DELETE',
                success: function() {
                    showToast('Success', 'Blog deleted successfully');
                    loadBlogs();
                },
                error: function() {
                    showToast('Error', 'Failed to delete blog');
                }
            });
        }
    });

    // Helper Functions
    function loadBlogs() {
        $.get('/admin/blogs/list', {
            page: currentPage,
            size: pageSize,
            keyword: keyword
        }, function(response) {
            displayBlogs(response.content);
            updatePagination(response);
        });
    }

    function displayBlogs(blogs) {
        const tbody = $('#blogsTable tbody');
        tbody.empty();
        blogs.forEach(function(blog) {
            tbody.append(`
                <tr>
                    <td>${blog.id}</td>
                    <td>
                        <img src="${blog.imageUrl || '/admin/images/no-image.png'}" 
                             alt="Blog thumbnail" 
                             class="img-thumbnail" 
                             style="max-width: 100px">
                    </td>
                    <td>${blog.title}</td>
                    <td>${blog.shortDescription}</td>
                    <td>${blog.authorName || 'Unknown'}</td>
                    <td>
                        <span class="badge ${blog.published ? 'bg-success' : 'bg-warning'}">
                            ${blog.published ? 'Published' : 'Draft'}
                        </span>
                    </td>
                    <td>${formatDate(blog.createdAt)}</td>
                    <td>
                        <button class="btn btn-sm btn-primary edit-blog" data-id="${blog.id}">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-sm btn-danger delete-blog" data-id="${blog.id}">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `);
        });
    }

    function updatePagination(response) {
        currentPage = response.currentPage;
        totalPages = response.totalPages;
        $('#currentPage').text(currentPage + 1);
        $('#totalPages').text(totalPages);
        $('#prevPage').prop('disabled', currentPage === 0);
        $('#nextPage').prop('disabled', currentPage === totalPages - 1);
    }

    function resetForm(formId) {
        $(formId)[0].reset();
        if (formId === '#addBlogForm') {
            $('#content').summernote('code', '');
        } else if (formId === '#editBlogForm') {
            $('#editContent').summernote('code', '');
            $('#currentImage').attr('src', '');
        }
    }

    function formatDate(dateString) {
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    function showToast(title, message) {
        $('.toast-header strong').text(title);
        $('.toast-body').text(message);
        $('.toast').toast('show');
    }
}); 