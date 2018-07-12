    $(document).ready(function() {
        var dt = $('#constructor-table').DataTable( {
            "columns": 
            [
                {
                    "orderable":      false,
                },
                { "data": "visibility" },
                { "data": "static" },
                { "data": "return" },
                { "data": "name" },
                { "data": "parameters" },
                { "data": "abstract" },
                { "data": "override" },
                { "data": "externally_used" },
                { "bSearchable": false, "orderable": false, "data": "cfg" },
                { "bSearchable": false, "orderable": false, "data": "call" },
                { "bSearchable": false, "orderable": false, "data": "usage_example" },
                { "orderable": false, "data": "comments" }
            ],
            "order": [[1, 'asc']]
        } );


     
        // Array to track the ids of the details displayed rows
        var detailRows = [];
     
        $('#constructor-table tbody').on( 'click', 'tr td.details-control', function () {
            var tr = $(this).closest('tr');
            var row = dt.row( tr );
            var idx = $.inArray( tr.attr('id'), detailRows );
     
            if ( row.child.isShown() ) {
                tr.removeClass( 'details' );
                row.child.hide();
     
                // Remove from the 'open' array
                detailRows.splice( idx, 1 );
            }
            else {
                tr.addClass( 'details' );
                row.child( row.data().comments ).show();
     
                // Add to the 'open' array
                if ( idx === -1 ) {
                    detailRows.push( tr.attr('id') );
                }
            }
        } );
     
        // On each draw, loop over the `detailRows` array and show any child rows
        dt.on( 'draw', function () {
            $.each( detailRows, function ( i, id ) {
                $('#'+id+' td.details-control').trigger( 'click' );
            } );
        } );
    } ); 