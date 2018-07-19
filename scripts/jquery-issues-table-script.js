    $(document).ready(function() {
        var dt = $('#issues-table').DataTable( {
            "lengthMenu": [[10, 25, 50, -1], [10, 25, 50, "All"]],
            "columns": 
            [
                {
                    "orderable":      false,
                },
                { "data": "bugId" },
                { "data": "last_changed" },
                { "data": "summary" },
                { "data": "status" },
                { "data": "severity" },
                { "data": "priority" },
                { "data": "commits" },
                { "bSearchable": false, "orderable": false, "data": "bug_report" },
            ],
            "order": [[1, 'asc']]
        } );

        var detailRows = [];
     
        $('#issues-table tbody').on( 'click', 'tr td.details-control', function () {
            var tr = $(this).closest('tr');
            var row = dt.row( tr );
            var idx = $.inArray( tr.attr('id'), detailRows );
     
            if ( row.child.isShown() ) {
                tr.removeClass( 'details' );
                row.child.hide();
     
                detailRows.splice( idx, 1 );
            }
            else {
                tr.addClass( 'details' );
                row.child( row.data().summary ).show();
     
                if ( idx === -1 ) {
                    detailRows.push( tr.attr('id') );
                }
            }
        } );
     
        dt.on( 'draw', function () {
            $.each( detailRows, function ( i, id ) {
                $('#'+id+' td.details-control').trigger( 'click' );
            } );
        } );
    } );  